package com.champions_Call.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.champions_Call.Form.Managing_Score.CricketMatchStateDTO;
import com.champions_Call.Form.Managing_Score.CricketScoringRequest;
import com.champions_Call.Helper.MatchStatus;
import com.champions_Call.model.Manging_Scoring.Cricket.CricketBallEvent;
import com.champions_Call.model.Manging_Scoring.Cricket.CricketInnings;
import com.champions_Call.model.Manging_Scoring.Cricket.CricketMatch;
import com.champions_Call.model.Manging_Scoring.Cricket.CricketPlayerStats;
import com.champions_Call.model.Team.TeamRegistration;
import com.champions_Call.model.Team.TeamRoster;
import com.champions_Call.model.match.Match;
import com.champions_Call.model.player.PlayerProfile;
import com.champions_Call.repository.Manging_Score_Cricket.CricketBallEventRepository;
import com.champions_Call.repository.Manging_Score_Cricket.CricketInningsRepository;
import com.champions_Call.repository.Managing_Scoring.CricketMatchRepository;
import com.champions_Call.repository.Manging_Score_Cricket.CricketPlayerStatsRepository;
import com.champions_Call.service.CricketScoringService;
import com.champions_Call.service.MatchService;

@Service
public class CricketScoringServiceImpl implements CricketScoringService {

    private final MatchService matchService;
    private final CricketMatchRepository cricketMatchRepository;
    private final CricketInningsRepository cricketInningsRepository;
    private final CricketPlayerStatsRepository cricketPlayerStatsRepository;
    private final CricketBallEventRepository cricketBallEventRepository;

    public CricketScoringServiceImpl(
            CricketMatchRepository cricketMatchRepository,
            CricketInningsRepository cricketInningsRepository,
            CricketPlayerStatsRepository cricketPlayerStatsRepository,
            CricketBallEventRepository cricketBallEventRepository,
            MatchService matchService) {

        this.cricketMatchRepository = cricketMatchRepository;
        this.cricketInningsRepository = cricketInningsRepository;
        this.cricketPlayerStatsRepository = cricketPlayerStatsRepository;
        this.cricketBallEventRepository = cricketBallEventRepository;
        this.matchService = matchService;
    }

    // ---------------------------------------------------------
    // MAIN METHOD
    // ---------------------------------------------------------

    @Override
    @Transactional
    public CricketMatchStateDTO processScoringEvent(CricketScoringRequest request) {

        validateScoringRequest(request);

        Match match = matchService.getById(request.matchId());

        if (match == null) {
            throw new IllegalArgumentException("Match not found");
        }

        if (match.getStatus() != MatchStatus.LIVE) {
            throw new IllegalStateException(
                    "Scoring is allowed only for live matches");
        }

        CricketMatch cricketMatch = getCricketMatch(request.matchId());
        CricketInnings innings = getCurrentInnings(cricketMatch);

        if (innings.getStatus() == CricketInnings.InningsStatus.COMPLETED) {
            throw new IllegalStateException("This innings is already completed");
        }

        switch (request.actionType()) {

            case BALL:
                processBall(innings, request);
                break;

            case SWAP_STRIKE:
                swapStrike(innings);
                break;

            case UNDO:
                undoLastBall(innings, request);
                break;

            default:
                throw new IllegalArgumentException("Unsupported scoring action");
        }

        cricketInningsRepository.save(innings);

        return buildMatchState(match, cricketMatch, innings);
    }

    // ---------------------------------------------------------
    // FETCH MATCH AND INNINGS
    // ---------------------------------------------------------

    private CricketMatch getCricketMatch(Long matchId) {

        return cricketMatchRepository.findById(matchId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Cricket match not found for match ID: " + matchId));
    }

    private CricketInnings getCurrentInnings(CricketMatch cricketMatch) {

        return cricketInningsRepository
                .findByCricketMatch_IdAndInningsNumber(
                        cricketMatch.getId(),
                        cricketMatch.getCurrentInningsNumber())
                .orElseThrow(() ->
                        new IllegalArgumentException("Current innings not found"));
    }

    // ---------------------------------------------------------
    // PROCESS A BALL
    // ---------------------------------------------------------

    private void processBall(
            CricketInnings innings,
            CricketScoringRequest request) {

        validateBallRequest(innings, request);

        // Prevent duplicate frontend submissions.
        if (cricketBallEventRepository.findByEventId(request.eventId()).isPresent()) {
            throw new IllegalArgumentException("This scoring event was already processed");
        }

        Long strikerId = innings.getStrikerId();
        Long nonStrikerId = innings.getNonStrikerId();
        Long bowlerId = innings.getCurrentBowlerId();

        PlayerProfile striker = findPlayer(innings, strikerId);
        PlayerProfile bowler = findPlayer(innings, bowlerId);

        int totalRuns = calculateTotalRuns(request);
        boolean legalDelivery = isLegalDelivery(request);

        // Save the event BEFORE updating legal balls, so over/ball numbers are correct.
        saveBallEvent(
                innings,
                request,
                strikerId,
                nonStrikerId,
                bowlerId);

        // Update innings score.
        innings.setTotalRuns(innings.getTotalRuns() + totalRuns);
        innings.setTotalExtras(
                innings.getTotalExtras() + request.extraRuns());

        if (legalDelivery) {
            innings.setLegalBalls(innings.getLegalBalls() + 1);
        }

        updateExtras(innings, request);

        // Update batter statistics.
        updateBattingStats(innings, request, striker);

        // Update bowler statistics.
        updateBowlingStats(innings, request, bowler);

        // Update wicket information.
        if (request.wicket()) {

            innings.setTotalWickets(innings.getTotalWickets() + 1);

            updateDismissedBatter(innings, request);

            updateBowlerWickets(innings, request, bowler);
        }

        // Rotate strike based on runs scored.
        if (totalRuns % 2 != 0) {
            swapStrikeIds(innings);
        }

        // Replace dismissed batter with incoming batter.
        if (request.wicket()) {
            replaceDismissedBatter(innings, request);
        }

        // At the end of an over, strike changes.
        if (legalDelivery && innings.getLegalBalls() % 6 == 0) {
            swapStrikeIds(innings);
        }

        updateInningsStatus(innings);

        cricketInningsRepository.save(innings);
    }

    // ---------------------------------------------------------
    // SAVE BALL EVENT
    // ---------------------------------------------------------

    private CricketBallEvent saveBallEvent(
            CricketInnings innings,
            CricketScoringRequest request,
            Long strikerId,
            Long nonStrikerId,
            Long bowlerId) {

        CricketBallEvent event = new CricketBallEvent();

        event.setEventId(request.eventId());
        event.setInnings(innings);

        event.setOverNumber((innings.getLegalBalls() / 6) + 1);
        event.setBallNumber((innings.getLegalBalls() % 6) + 1);

        event.setEventSequence(getNextEventSequence(innings));

        event.setStrikerId(strikerId);
        event.setNonStrikerId(nonStrikerId);
        event.setBowlerId(bowlerId);

        event.setRunsOffBat(request.runsOffBat());
        event.setCompletedRuns(request.completedRuns());

        // FIX: this was setExtraRuns(...) with an enum; it must be setExtraType(...)
        // and both branches of the ternary must be the entity enum.
        event.setExtraType(
                request.extraType() == null
                        ? CricketBallEvent.ExtraType.NONE
                        : CricketBallEvent.ExtraType.valueOf(
                                request.extraType().name()));

        event.setExtraRuns(request.extraRuns());

        event.setWicket(request.wicket());

        if (request.wicketType() != null) {
            event.setWicketType(
                    CricketBallEvent.WicketType.valueOf(
                            request.wicketType().name()));
        }

        event.setDismissedBatterId(request.dismissedBatterId());
        event.setNewBatterId(request.newBatterId());
        event.setUndone(false);

        return cricketBallEventRepository.save(event);
    }

    private long getNextEventSequence(CricketInnings innings) {

        return cricketBallEventRepository
                .findByInnings_IdOrderByEventSequenceAsc(innings.getId())
                .stream()
                .mapToLong(CricketBallEvent::getEventSequence)
                .max()
                .orElse(0L) + 1;
    }

    // ---------------------------------------------------------
    // RUN CALCULATIONS
    // ---------------------------------------------------------

    private int calculateTotalRuns(CricketScoringRequest request) {

        return request.runsOffBat() + request.extraRuns();
    }

    private boolean isLegalDelivery(CricketScoringRequest request) {

        return request.extraType() != CricketScoringRequest.ExtraType.WIDE
                && request.extraType() != CricketScoringRequest.ExtraType.NO_BALL;
    }

    // ---------------------------------------------------------
    // PLAYER LOOKUP
    // ---------------------------------------------------------

    private PlayerProfile findPlayer(
            CricketInnings innings,
            Long playerId) {

        if (playerId == null) {
            throw new IllegalArgumentException("Player ID is required");
        }

        PlayerProfile player = findPlayerInTeam(
                innings.getBattingTeam(), playerId);

        if (player == null) {
            player = findPlayerInTeam(
                    innings.getBowlingTeam(), playerId);
        }

        if (player == null) {
            throw new IllegalArgumentException(
                    "Player " + playerId + " is not part of this match");
        }

        return player;
    }

    private PlayerProfile findPlayerInTeam(
            TeamRegistration team,
            Long playerId) {

        if (team == null || team.getRoster() == null) {
            return null;
        }

        for (TeamRoster roster : team.getRoster()) {

            PlayerProfile player = roster.getPlayerProfile();

            if (player != null && playerId.equals(player.getId())) {
                return player;
            }
        }

        return null;
    }

    private CricketPlayerStats getOrCreatePlayerStats(
            CricketInnings innings,
            PlayerProfile player) {

        return cricketPlayerStatsRepository
                .findByInnings_IdAndPlayer_Id(
                        innings.getId(),
                        player.getId())
                .orElseGet(() -> {

                    CricketPlayerStats stats = new CricketPlayerStats();

                    stats.setInnings(innings);
                    stats.setPlayer(player);

                    return cricketPlayerStatsRepository.save(stats);
                });
    }

    // ---------------------------------------------------------
    // BATTING STATISTICS
    // ---------------------------------------------------------

    private void updateBattingStats(
            CricketInnings innings,
            CricketScoringRequest request,
            PlayerProfile striker) {

        CricketPlayerStats stats =
                getOrCreatePlayerStats(innings, striker);

        stats.setRuns(stats.getRuns() + request.runsOffBat());

        if (isLegalDelivery(request)) {
            stats.setBallsFaced(stats.getBallsFaced() + 1);
        }

        if (request.runsOffBat() == 4) {
            stats.setFours(stats.getFours() + 1);
        }

        if (request.runsOffBat() == 6) {
            stats.setSixes(stats.getSixes() + 1);
        }

        cricketPlayerStatsRepository.save(stats);
    }

    // ---------------------------------------------------------
    // BOWLING STATISTICS
    // ---------------------------------------------------------

    private void updateBowlingStats(
            CricketInnings innings,
            CricketScoringRequest request,
            PlayerProfile bowler) {

        CricketPlayerStats stats =
                getOrCreatePlayerStats(innings, bowler);

        if (isLegalDelivery(request)) {
            stats.setLegalBallsBowled(
                    stats.getLegalBallsBowled() + 1);
        }

        int runsChargedToBowler = request.runsOffBat();

        if (request.extraType() == CricketScoringRequest.ExtraType.WIDE
                || request.extraType() == CricketScoringRequest.ExtraType.NO_BALL) {

            runsChargedToBowler += request.extraRuns();
        }

        stats.setRunsConceded(
                stats.getRunsConceded() + runsChargedToBowler);

        cricketPlayerStatsRepository.save(stats);
    }

    private void updateBowlerWickets(
            CricketInnings innings,
            CricketScoringRequest request,
            PlayerProfile bowler) {

        if (!isBowlerCreditedWicket(request.wicketType())) {
            return;
        }

        CricketPlayerStats stats =
                getOrCreatePlayerStats(innings, bowler);

        stats.setWickets(stats.getWickets() + 1);

        cricketPlayerStatsRepository.save(stats);
    }

    /**
     * Works with both CricketScoringRequest.WicketType and
     * CricketBallEvent.WicketType by comparing enum names.
     */
    private boolean isBowlerCreditedWicket(Enum<?> wicketType) {

        if (wicketType == null) {
            return false;
        }

        return switch (wicketType.name()) {
            case "BOWLED", "CAUGHT", "LBW", "STUMPED", "HIT_WICKET" -> true;
            default -> false;
        };
    }

    // ---------------------------------------------------------
    // WICKET HANDLING
    // ---------------------------------------------------------

    private void updateDismissedBatter(
            CricketInnings innings,
            CricketScoringRequest request) {

        PlayerProfile dismissedPlayer =
                findPlayer(innings, request.dismissedBatterId());

        CricketPlayerStats stats =
                getOrCreatePlayerStats(innings, dismissedPlayer);

        stats.setDismissed(true);
        stats.setDismissalType(request.wicketType().name());

        cricketPlayerStatsRepository.save(stats);
    }

    private void replaceDismissedBatter(
            CricketInnings innings,
            CricketScoringRequest request) {

        Long dismissedId = request.dismissedBatterId();
        Long newBatterId = request.newBatterId();

        if (newBatterId == null) {
            throw new IllegalArgumentException(
                    "New batter ID is required after a wicket");
        }

        findPlayer(innings, newBatterId);

        if (dismissedId.equals(innings.getStrikerId())) {
            innings.setStrikerId(newBatterId);
        } else if (dismissedId.equals(innings.getNonStrikerId())) {
            innings.setNonStrikerId(newBatterId);
        } else {
            throw new IllegalArgumentException(
                    "Dismissed batter is not currently at the crease");
        }
    }

    // ---------------------------------------------------------
    // EXTRAS
    // ---------------------------------------------------------

    private void updateExtras(
            CricketInnings innings,
            CricketScoringRequest request) {

        if (request.extraType() == null) {
            return;
        }

        switch (request.extraType()) {

            case WIDE:
                innings.setWides(
                        innings.getWides() + request.extraRuns());
                break;

            case NO_BALL:
                innings.setNoBalls(
                        innings.getNoBalls() + request.extraRuns());
                break;

            case BYE:
                innings.setByes(
                        innings.getByes() + request.extraRuns());
                break;

            case LEG_BYE:
                innings.setLegByes(
                        innings.getLegByes() + request.extraRuns());
                break;

            case NONE:
                break;
        }
    }

    // ---------------------------------------------------------
    // STRIKE MANAGEMENT
    // ---------------------------------------------------------

    private void swapStrike(CricketInnings innings) {

        if (innings.getStrikerId() == null
                || innings.getNonStrikerId() == null) {

            throw new IllegalStateException(
                    "Both striker and non-striker must be selected");
        }

        swapStrikeIds(innings);
        cricketInningsRepository.save(innings);
    }

    private void swapStrikeIds(CricketInnings innings) {

        Long striker = innings.getStrikerId();

        innings.setStrikerId(innings.getNonStrikerId());
        innings.setNonStrikerId(striker);
    }

    // ---------------------------------------------------------
    // UNDO LATEST BALL
    // ---------------------------------------------------------

    private void undoLastBall(
            CricketInnings innings,
            CricketScoringRequest request) {

        if (request.targetEventId() == null
                || request.targetEventId().isBlank()) {

            throw new IllegalArgumentException(
                    "Target event ID is required for undo");
        }

        CricketBallEvent event = cricketBallEventRepository
                .findByEventId(request.targetEventId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Scoring event not found"));

        if (!event.getInnings().getId().equals(innings.getId())) {
            throw new IllegalArgumentException(
                    "Event does not belong to the current innings");
        }

        if (event.isUndone()) {
            throw new IllegalStateException("This event has already been undone");
        }

        CricketBallEvent latestEvent = cricketBallEventRepository
                .findByInnings_IdOrderByEventSequenceAsc(innings.getId())
                .stream()
                .filter(e -> !e.isUndone())
                .max((a, b) ->
                        Long.compare(a.getEventSequence(), b.getEventSequence()))
                .orElseThrow(() ->
                        new IllegalStateException("No active ball event to undo"));

        if (!latestEvent.getId().equals(event.getId())) {
            throw new IllegalStateException(
                    "Only the latest active ball can be undone");
        }

        reverseBallEvent(innings, event);

        // Restore the players who were at the crease before the ball.
        innings.setStrikerId(event.getStrikerId());
        innings.setNonStrikerId(event.getNonStrikerId());
        innings.setCurrentBowlerId(event.getBowlerId());

        event.setUndone(true);
        cricketBallEventRepository.save(event);

        updateInningsStatus(innings);
        cricketInningsRepository.save(innings);
    }

    private void reverseBallEvent(
            CricketInnings innings,
            CricketBallEvent event) {

        innings.setTotalRuns(
                innings.getTotalRuns()
                        - event.getRunsOffBat()
                        - event.getExtraRuns());

        innings.setTotalExtras(
                innings.getTotalExtras() - event.getExtraRuns());

        if (event.getExtraType() != null) {

            switch (event.getExtraType()) {

                case WIDE:
                    innings.setWides(
                            innings.getWides() - event.getExtraRuns());
                    break;

                case NO_BALL:
                    innings.setNoBalls(
                            innings.getNoBalls() - event.getExtraRuns());
                    break;

                case BYE:
                    innings.setByes(
                            innings.getByes() - event.getExtraRuns());
                    break;

                case LEG_BYE:
                    innings.setLegByes(
                            innings.getLegByes() - event.getExtraRuns());
                    break;

                case NONE:
                    break;
            }
        }

        boolean legal = event.getExtraType() != CricketBallEvent.ExtraType.WIDE
                && event.getExtraType() != CricketBallEvent.ExtraType.NO_BALL;

        if (legal) {
            innings.setLegalBalls(
                    Math.max(0, innings.getLegalBalls() - 1));
        }

        PlayerProfile striker =
                findPlayer(innings, event.getStrikerId());

        CricketPlayerStats batterStats =
                getOrCreatePlayerStats(innings, striker);

        batterStats.setRuns(
                Math.max(0, batterStats.getRuns() - event.getRunsOffBat()));

        if (legal) {
            batterStats.setBallsFaced(
                    Math.max(0, batterStats.getBallsFaced() - 1));
        }

        if (event.getRunsOffBat() == 4) {
            batterStats.setFours(
                    Math.max(0, batterStats.getFours() - 1));
        }

        if (event.getRunsOffBat() == 6) {
            batterStats.setSixes(
                    Math.max(0, batterStats.getSixes() - 1));
        }

        cricketPlayerStatsRepository.save(batterStats);

        PlayerProfile bowler =
                findPlayer(innings, event.getBowlerId());

        CricketPlayerStats bowlerStats =
                getOrCreatePlayerStats(innings, bowler);

        if (legal) {
            bowlerStats.setLegalBallsBowled(
                    Math.max(0, bowlerStats.getLegalBallsBowled() - 1));
        }

        int runsChargedToBowler = event.getRunsOffBat();

        if (event.getExtraType() == CricketBallEvent.ExtraType.WIDE
                || event.getExtraType() == CricketBallEvent.ExtraType.NO_BALL) {

            runsChargedToBowler += event.getExtraRuns();
        }

        bowlerStats.setRunsConceded(
                Math.max(0,
                        bowlerStats.getRunsConceded() - runsChargedToBowler));

        if (event.isWicket()) {

            innings.setTotalWickets(
                    Math.max(0, innings.getTotalWickets() - 1));

            if (event.getDismissedBatterId() != null) {

                PlayerProfile dismissedPlayer =
                        findPlayer(innings, event.getDismissedBatterId());

                CricketPlayerStats dismissedStats =
                        getOrCreatePlayerStats(innings, dismissedPlayer);

                dismissedStats.setDismissed(false);
                dismissedStats.setDismissalType(null);

                cricketPlayerStatsRepository.save(dismissedStats);
            }

            if (isBowlerCreditedWicket(event.getWicketType())) {
                bowlerStats.setWickets(
                        Math.max(0, bowlerStats.getWickets() - 1));
            }
        }

        cricketPlayerStatsRepository.save(bowlerStats);
    }

    // ---------------------------------------------------------
    // INNINGS STATUS
    // ---------------------------------------------------------

    private void updateInningsStatus(CricketInnings innings) {

        if (innings.getTotalWickets() >= 10
                || (innings.getTarget() != null
                    && innings.getTotalRuns() >= innings.getTarget())) {

            innings.setStatus(
                    CricketInnings.InningsStatus.COMPLETED);

        } else {
            innings.setStatus(
                    CricketInnings.InningsStatus.IN_PROGRESS);
        }
    }

    // ---------------------------------------------------------
    // MATCH STATE DTO
    // ---------------------------------------------------------

    private CricketMatchStateDTO buildMatchState(
            Match match,
            CricketMatch cricketMatch,
            CricketInnings innings) {

        CricketPlayerStats strikerStats =
                getStatsForPlayer(innings, innings.getStrikerId());

        CricketPlayerStats nonStrikerStats =
                getStatsForPlayer(innings, innings.getNonStrikerId());

        CricketPlayerStats bowlerStats =
                getStatsForPlayer(innings, innings.getCurrentBowlerId());

        CricketMatchStateDTO.Batter striker =
                buildBatter(innings, innings.getStrikerId(), strikerStats);

        CricketMatchStateDTO.Batter nonStriker =
                buildBatter(innings, innings.getNonStrikerId(), nonStrikerStats);

        CricketMatchStateDTO.Bowler bowler =
                buildBowler(innings, innings.getCurrentBowlerId(), bowlerStats);

        List<String> recentBalls = buildRecentBalls(innings);

        return new CricketMatchStateDTO(
                match.getId(),
                match.getStatus().name(),
                innings.getBattingTeam().getTeamName(),
                innings.getBowlingTeam().getTeamName(),
                innings.getTotalRuns(),
                innings.getTotalWickets(),
                innings.getLegalBalls() / 6,
                innings.getLegalBalls() % 6,
                calculateRunRate(innings),
                innings.getTarget() == null ? 0 : innings.getTarget(),
                calculateRunsRequired(innings),
                striker,
                nonStriker,
                bowler,
                recentBalls,
                innings.getTotalExtras(),
                innings.getWides(),
                innings.getNoBalls(),
                innings.getByes(),
                innings.getLegByes(),
                cricketMatch.getCurrentInningsNumber()
        );
    }

    private CricketPlayerStats getStatsForPlayer(
            CricketInnings innings,
            Long playerId) {

        if (playerId == null) {
            return null;
        }

        return cricketPlayerStatsRepository
                .findByInnings_IdAndPlayer_Id(
                        innings.getId(), playerId)
                .orElse(null);
    }

    private CricketMatchStateDTO.Batter buildBatter(
            CricketInnings innings,
            Long playerId,
            CricketPlayerStats stats) {

        if (playerId == null) {
            return null;
        }

        PlayerProfile player = findPlayer(innings, playerId);

        int runs = stats == null ? 0 : stats.getRuns();
        int balls = stats == null ? 0 : stats.getBallsFaced();
        int fours = stats == null ? 0 : stats.getFours();
        int sixes = stats == null ? 0 : stats.getSixes();

        double strikeRate = balls == 0
                ? 0.0
                : (runs * 100.0) / balls;

        return new CricketMatchStateDTO.Batter(
                player.getId(),
                player.getFullName(),
                runs,
                balls,
                fours,
                sixes,
                strikeRate
        );
    }

    private CricketMatchStateDTO.Bowler buildBowler(
            CricketInnings innings,
            Long playerId,
            CricketPlayerStats stats) {

        if (playerId == null) {
            return null;
        }

        PlayerProfile player = findPlayer(innings, playerId);

        int legalBalls = stats == null ? 0 : stats.getLegalBallsBowled();
        int runs = stats == null ? 0 : stats.getRunsConceded();
        int wickets = stats == null ? 0 : stats.getWickets();
        int maidens = stats == null ? 0 : stats.getMaidens();

        double economy = legalBalls == 0
                ? 0.0
                : (runs * 6.0) / legalBalls;

        return new CricketMatchStateDTO.Bowler(
                player.getId(),
                player.getFullName(),
                legalBalls / 6,
                legalBalls % 6,
                runs,
                wickets,
                maidens,
                economy
        );
    }

    private List<String> buildRecentBalls(CricketInnings innings) {

        List<CricketBallEvent> events =
                cricketBallEventRepository
                        .findByInnings_IdOrderByEventSequenceAsc(innings.getId());

        List<String> recent = new ArrayList<>();

        for (CricketBallEvent event : events) {

            if (event.isUndone()) {
                continue;
            }

            String label;

            if (event.isWicket()) {
                label = "W";
            } else if (event.getExtraType() == CricketBallEvent.ExtraType.WIDE) {
                label = event.getExtraRuns() + "Wd";
            } else if (event.getExtraType() == CricketBallEvent.ExtraType.NO_BALL) {
                label = event.getExtraRuns() + "Nb";
            } else if (event.getExtraType() == CricketBallEvent.ExtraType.BYE) {
                label = event.getExtraRuns() + "B";
            } else if (event.getExtraType() == CricketBallEvent.ExtraType.LEG_BYE) {
                label = event.getExtraRuns() + "Lb";
            } else {
                label = String.valueOf(event.getRunsOffBat());
            }

            recent.add(label);
        }

        int fromIndex = Math.max(0, recent.size() - 6);

        return new ArrayList<>(recent.subList(fromIndex, recent.size()));
    }

    // ---------------------------------------------------------
    // RUN RATE
    // ---------------------------------------------------------

    private double calculateRunRate(CricketInnings innings) {

        if (innings.getLegalBalls() == 0) {
            return 0.0;
        }

        return (innings.getTotalRuns() * 6.0)
                / innings.getLegalBalls();
    }

    private int calculateRunsRequired(CricketInnings innings) {

        if (innings.getTarget() == null) {
            return 0;
        }

        return Math.max(
                0,
                innings.getTarget() - innings.getTotalRuns());
    }

    // ---------------------------------------------------------
    // VALIDATION
    // ---------------------------------------------------------

    private void validateScoringRequest(CricketScoringRequest request) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Scoring request cannot be null");
        }

        if (request.matchId() == null) {
            throw new IllegalArgumentException("Match ID is required");
        }

        if (request.actionType() == null) {
            throw new IllegalArgumentException("Action type is required");
        }

        if (request.eventId() == null || request.eventId().isBlank()) {
            throw new IllegalArgumentException("Event ID is required");
        }

        if (request.actionType() == CricketScoringRequest.ActionType.UNDO
                && (request.targetEventId() == null
                    || request.targetEventId().isBlank())) {

            throw new IllegalArgumentException(
                    "Target event ID is required for undo");
        }
    }

    private void validateBallRequest(
            CricketInnings innings,
            CricketScoringRequest request) {

        if (innings.getStrikerId() == null
                || innings.getNonStrikerId() == null
                || innings.getCurrentBowlerId() == null) {

            throw new IllegalStateException(
                    "Select striker, non-striker and bowler before scoring");
        }

        if (request.runsOffBat() < 0 || request.extraRuns() < 0
                || request.completedRuns() < 0) {

            throw new IllegalArgumentException(
                    "Runs cannot be negative");
        }

        if (request.extraType() == null) {
            throw new IllegalArgumentException(
                    "Extra type is required. Use NONE when there are no extras");
        }

        if (request.wicket()) {

            if (request.wicketType() == null) {
                throw new IllegalArgumentException(
                        "Wicket type is required");
            }

            if (request.dismissedBatterId() == null) {
                throw new IllegalArgumentException(
                        "Dismissed batter ID is required");
            }

            if (!request.dismissedBatterId().equals(innings.getStrikerId())
                    && !request.dismissedBatterId().equals(
                            innings.getNonStrikerId())) {

                throw new IllegalArgumentException(
                        "Dismissed batter must be one of the current batters");
            }

            if (request.newBatterId() == null) {
                throw new IllegalArgumentException(
                        "New batter ID is required");
            }
        }
    }

   // @Transactional(readOnly = true) : 
    // Tells Spring this method only reads from the database. It never saves or updates, so it’s slightly faster and safer.
    @Override
    @Transactional 
    public CricketMatchStateDTO getMatchState(Long matchId){

        // Your processScoringEvent() ->  can only build the scoreboard after a scorer sends a ball. Think about a viewer who opens the match page in the middle of the match:

        // They have missed all previous WebSocket messages.
        // Nothing arrives until the scorer enters the next ball.
        // So they would see an empty scoreboard until then.

        // getMatchState() fixes that:

        // The viewer opens the page → the browser calls GET /api/public/matches/{id}/cricket/state → shows the current score immediately.
        // The browser then subscribes to /topic/matches/{id}/cricket → the score keeps updating live from there.

        Match match = matchService.getById(matchId);
        CricketMatch cricketMatch = getCricketMatch(matchId);
        CricketInnings innings = getCurrentInnings(cricketMatch);
        return buildMatchState(match, cricketMatch, innings);
    }
}