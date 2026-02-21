package com.backend.givr.shared.service;

import com.backend.givr.organization.dtos.LocationDto;
import com.backend.givr.shared.entity.Location;
import com.backend.givr.shared.repo.LocationRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LocationService {
    @Autowired
    private LocationRepo repo;

    public Location createLocation(Location location){
        if(location == null)
            return null;
        Optional<Location> location1 = repo.findByStateAndLga(location.getState(), location.getLga());
        return location1.orElseGet(() -> repo.save(location));
    }

    public Location createLocation(LocationDto locationDto){
        if(locationDto == null)
            return null;
        Location location = new Location(locationDto);
       return createLocation(location);
    }
}
