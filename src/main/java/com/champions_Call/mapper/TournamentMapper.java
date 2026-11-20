package com.champions_Call.mapper;

import org.springframework.stereotype.Component;

import com.champions_Call.Form.organizer.TournamentDTO;
import com.champions_Call.model.Tournament.Tournament;
import com.champions_Call.model.Tournament.TournamentRule;
import com.champions_Call.model.organizer.Prize;

@Component
public class TournamentMapper {

  public TournamentDTO toDTO(Tournament t){
     TournamentDTO dto = new TournamentDTO();
        
        // Using 'if' checks to ensure we only map what exists
        if (t.getId() != null) dto.setId(t.getId());
        if (t.getTournamentName() != null) dto.setTournamentName(t.getTournamentName());

        if (t.getTournamentType() != null) dto.setTournamentType(t.getTournamentType());

        if (t.getSport() != null) dto.setSport(t.getSport());

        if (t.getBallType() != null) dto.setBallType(t.getBallType());

        if (t.getVenueLocation() != null) dto.setVenueLocation(t.getVenueLocation());

        if (t.getStartDate() != null) dto.setStartDate(t.getStartDate());
        if (t.getEndDate() != null) dto.setEndDate(t.getEndDate());
        if (t.getEntryFee() != null) dto.setEntryFee(t.getEntryFee());
        if (t.getMaxTeamsAllowed() != null) dto.setMaxTeamsAllowed(t.getMaxTeamsAllowed());
        if(t.getDescription()!=null){
            dto.setDescription(dto.getDescription());
        }
        if (t.getRegistrationStartDate() != null) dto.setRegistrationStartDate(t.getRegistrationStartDate());
        if (t.getRegistrationDeadline() != null) dto.setRegistrationDeadline(t.getRegistrationDeadline());
        if (t.getPlayerCategory() != null) dto.setPlayerCategory(t.getPlayerCategory());
        if (t.getTournamentStyle() != null) dto.setTournamentStyle(t.getTournamentStyle());
        if (t.getMinSquadSize() != null) dto.setMinSquadSize(t.getMinSquadSize());
        if (t.getMaxSquadSize() != null) dto.setMaxSquadSize(t.getMaxSquadSize());

        if (dto.getCustomRules() != null && !dto.getCustomRules().isEmpty()) {
            t.getCustomRules().clear();
            for (TournamentRule dtoRule : dto.getCustomRules()) {
                TournamentRule rule = new TournamentRule();
                rule.setDescription(dtoRule.getDescription());
                rule.setTournament(t);
                t.getCustomRules().add(rule);
            }
        }

        if (dto.getPrizes() != null && !dto.getPrizes().isEmpty()) {
            t.getPrizes().clear();
            for (Prize dtoPrize : dto.getPrizes()) {
                Prize prize = new Prize();
                prize.setPrizeName(dtoPrize.getPrizeName());
                prize.setPrizeType(dtoPrize.getPrizeType());
                prize.setPrizeDetails(dtoPrize.getPrizeDetails());
                prize.setTournament(t);
                t.getPrizes().add(prize);
            }
        }

        if (t.getCurrentStep() != null) dto.setCurrentStep(t.getCurrentStep());

        String sport = (t.getSport() != null) ? t.getSport().toLowerCase() : "";
        String filename;

        switch (sport) {
            case "cricket": filename = "cricket-2.png"; break;
            case "football": filename = "football-2.png"; break;
            case "kabaddi": filename = "kabaddi-2.png"; break;
            default: filename = "cricket-2.png"; // Default fallback
        }

        // 2. Set the path
        String path = (t.getBannerImageUrl() != null && !t.getBannerImageUrl().isEmpty()) 
                    ? t.getBannerImageUrl() 
                    : "/Images/" + filename;

        // 3. Store the final CSS string
        dto.setBackgroundStyle("background-image: url('" + path + "')");

        if (t.getTournamentAccess() != null) dto.setTournamentAccess(t.getTournamentAccess());

        return dto;
  }     
}
