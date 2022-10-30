package com.G2T7.OurGardenStory.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import java.util.*;

import javax.management.relation.Relation;
import javax.print.attribute.standard.MediaSize.ISO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import com.G2T7.OurGardenStory.controller.GeocodeService;
import com.G2T7.OurGardenStory.exception.CustomException;
import com.G2T7.OurGardenStory.model.ReqResModel.UserSignInResponse;
import com.G2T7.OurGardenStory.model.Garden;
import com.G2T7.OurGardenStory.model.User;
import com.G2T7.OurGardenStory.model.Window;
import com.G2T7.OurGardenStory.model.RelationshipModel.Ballot;
import com.G2T7.OurGardenStory.model.RelationshipModel.Relationship;
import com.G2T7.OurGardenStory.model.ReqResModel.UserSignInResponse;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import java.util.Base64;
import org.springframework.util.StringUtils;
import com.fasterxml.jackson.databind.JsonNode;

@Service
public class BallotService {
    
    @Autowired
    private DynamoDBMapper dynamoDBMapper;

    @Autowired
    private GardenService gardenService;

    @Autowired
    private UserService userService;

    @Autowired
    private GeocodeService geocodeService;

    @Autowired
    private WindowService windowService;

    @Autowired
    private RelationshipService relationshipService;

    public Relationship findUsernameInBallot(String windowId, String username) {
        String capWinId = StringUtils.capitalize(windowId);

        Relationship foundBallot = dynamoDBMapper.load(Ballot.class, capWinId, username);
        if (foundBallot == null) {
            throw new ResourceNotFoundException("Ballot does not exist.");
        } 
        return foundBallot;
    }

    public List<Relationship> addBallotInWindow(String windowId, String username, JsonNode payload) {
        String capWinId = StringUtils.capitalize(windowId);
        List<Relationship> toAddRelationshipList = new ArrayList<Relationship>();

        Garden garden = gardenService.findGardenByGardenName(payload.get("gardenName").asText());
        String latitude = garden.getLatitude();
        String longitude = garden.getLongitude();

        User user = userService.findUserByUsername(username);
        String userAddress = user.getAddress();

        double distance = geocodeService.saveDistance(username, userAddress, longitude, latitude);

        LocalDate date = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        String currentDate = date.format(formatter);

        //validations
        validateUser(username);
        validateBallotPostDate(windowId, date);
        validateGardenInWindow(windowId, currentDate);
        validateIfGardenFull(garden, capWinId);
        validateUserHasBallotedBeforeInSameWindow(windowId, username);

        payload.forEach(relation -> {
            Relationship ballot = new Ballot(capWinId, username, payload.get("gardenName").asText(), "Ballot" + String.valueOf(++Ballot.numInstance), currentDate, distance, "Pending");
            toAddRelationshipList.add(ballot);
        });
        
        dynamoDBMapper.batchSave(toAddRelationshipList);
        return toAddRelationshipList;
    }

    public Relationship updateGardenInBallot(String windowId, String username, JsonNode payload) throws Exception {
        String capWinId = StringUtils.capitalize(windowId);
        Relationship toUpdateBallot = findUsernameInBallot(capWinId, username);

        LocalDate date = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        String currentDate = date.format(formatter);

        toUpdateBallot = new Ballot(capWinId, username, payload.get("gardenName").asText(),  "Ballot" + String.valueOf(++Ballot.numInstance), currentDate, toUpdateBallot.getDistance(), "Pending");

        dynamoDBMapper.save(toUpdateBallot);
        return toUpdateBallot;
    }      

    public void deleteBallotInWindow(String windowId, String username) {
        String capWinId = StringUtils.capitalize(windowId);

        Relationship ballotToDelete = findUsernameInBallot(capWinId, username);
        dynamoDBMapper.delete(ballotToDelete);
    }

    public String getUsername() {
        String idToken = UserSignInResponse.getIdToken();
        String[] chunks = idToken.split("\\."); // chunk 0 is header, chunk 1 is payload, chunk 2 is signature

        Base64.Decoder decoder = Base64.getUrlDecoder(); // Decode via Base64
        String decodedPayload = new String(decoder.decode(chunks[1])); // gets payload from idToken
        String[] payload_attr = decodedPayload.split(",");

        for (String payload : payload_attr) {
            if (payload.contains("username")) {
                return payload.substring(payload.indexOf(":\"") + 2, payload.length() - 1); // username is returned
            }
        }
        return null;
    }

    public List<Relationship> findAllBallotsInWindowForGarden(String windowId, String gardenName) {
        String capWinId = StringUtils.capitalize(windowId);
        // if (!relationshipService.validateWinExist(capWinId)) {
        //   throw new ResourceNotFoundException(capWinId + " does not exist.");
        // }
        Map<String, AttributeValue> eav = new HashMap<String, AttributeValue>();
        eav.put(":WINID", new AttributeValue().withS(capWinId));
        DynamoDBQueryExpression<Relationship> qe = new DynamoDBQueryExpression<Relationship>()
            .withKeyConditionExpression("PK = :WINID").withExpressionAttributeValues(eav);
    
        PaginatedQueryList<Relationship> foundRelationList = dynamoDBMapper.query(Relationship.class, qe);
        if (foundRelationList.isEmpty() || foundRelationList == null) {
          throw new ResourceNotFoundException("There are no gardens in " + capWinId + ".");
        }
        return foundRelationList;
      }

    public void validateUser(String username) {
        User user = userService.findUserByUsername(username);
        String DOB = user.getDOB();
        LocalDate birthdate = LocalDate.of(Integer.parseInt(DOB.substring(6)), 
                                            Integer.parseInt(DOB.substring(3, 5)), Integer.parseInt(DOB.substring(0, 2)));
        
        birthdate = birthdate.minusYears(18);
        if (birthdate.getYear() < 0) {
            throw new CustomException("User must be 18 to ballot");
        }
        return;
    }

    public void validateBallotPostDate(String windowId, LocalDate date) {
        //TODO
        Window win = windowService.findWindowById(windowId).get(0);
        //LocalDate startDate = win.getSK();
        return;
    }

    public void validateGardenInWindow(String windowId, String gardenName) {
        relationshipService.findGardenInWindow(windowId, gardenName);
    }

    public void validateIfGardenFull(Garden garden, String winId) {
        //TODO
        return;
    }

    public void validateUserHasBallotedBeforeInSameWindow(String windowId, String username) {
        Relationship r = findUsernameInBallot(windowId, username);
        if (r != null) {
            throw new CustomException("User has already balloted in the same window");
        }
        return;
    }
}
