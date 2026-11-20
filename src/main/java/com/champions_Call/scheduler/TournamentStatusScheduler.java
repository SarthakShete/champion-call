package com.champions_Call.scheduler;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.champions_Call.model.Tournament.Tournament;
import com.champions_Call.model.Tournament.TournamentStatus;
import com.champions_Call.repository.TournamentRepository;

@Service
public class TournamentStatusScheduler {

    @Autowired
    private TournamentRepository tournamentRepo;


    @Scheduled(cron = "0 0/1 * * * ?")
    @Transactional
    public void updatedTournamenetStatues(){

        LocalDate today = LocalDate.now();
        List<Tournament> tournaments = tournamentRepo.findActiveTournaments();

        // Logic for REGISTRATION_OPEN and ONGOING
        for(Tournament t : tournaments){
           boolean statusChanged = false;

            if (t.getRegistrationStartDate() != null && t.getRegistrationDeadline() != null && 
            t.getStartDate() != null && t.getEndDate() != null) {

            if(t.getStatus() == TournamentStatus.UNPUBLISHED || t.getStatus() == TournamentStatus.REGISTRATION_OPEN ){

                // Transition to OPEN
                if (!today.isBefore(t.getRegistrationStartDate()) && !today.isAfter(t.getRegistrationDeadline())) {
                    if (t.getStatus() != TournamentStatus.REGISTRATION_OPEN) {
                        t.setStatus(TournamentStatus.REGISTRATION_OPEN);
                        statusChanged = true;
                    }
                } 
                // Transition to ONGOING
                else if (today.isAfter(t.getRegistrationDeadline()) && 
                        (today.isEqual(t.getStartDate()) || today.isAfter(t.getStartDate()))) {
                    t.setStatus(TournamentStatus.ONGOING);
                    statusChanged = true;
                }
            }
            
            // 2. Logic for COMPLETED
            if (today.isAfter(t.getEndDate()) && t.getStatus() != TournamentStatus.COMPLETED) {
                t.setStatus(TournamentStatus.COMPLETED);
                statusChanged = true;
            }
        }
            
            // Save only if something actually changed
            if (statusChanged) {
                tournamentRepo.save(t);
            }
        }
    }
}
