package com.backend.givr.shared.service;

import com.backend.givr.shared.Location;
import com.backend.givr.shared.repo.LocationRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LocationService {
    @Autowired
    private LocationRepo repo;

    public Location createLocation(Location location){
        Optional<Location> location1 = repo.findByStateAndLga(location.getState(), location.getLga());
        return location1.orElseGet(() -> repo.save(location));
    }
}
