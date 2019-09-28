package com.crypto.trader.dao;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.model.DeleteItemRequest;
import com.crypto.trader.models.TradeAction;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.context.annotation.Bean;

import javax.inject.Named;
import java.util.HashMap;
import java.util.List;

@Named
public class PriceDataDao {

    @Autowired
    JdbcTemplate template;

    public void putData(DateTime timestamp, double price){
        template.execute(String.format("INSERT INTO prices(time,price)\n" +
                "VALUES ('%S',%f)", timestamp,price));
    }

    public void deleteOldRecords(){
        template.execute("DELETE FROM prices WHERE time < NOW() - INTERVAL '1 day'\n");
    }

    public List<Double> getPrices(){
       return template.query("SELECT * FROM prices",(rs,rowNum)->rs.getDouble("price"));
    }

    public TradeAction getLastTrade(){
        return template.query("SELECT * FROM trades WHERE time = (SELECT MAX(time) FROM trades)",(rs,rowNum)->
            TradeAction.Builder().setAction(rs.getString("trade_action")).setPrice(rs.getDouble("price"))
        ).get(0);
    }

    public void writeTrade(DateTime timestamp, TradeAction action){
        template.execute(String.format("INSERT INTO trades(time,price,trade_action)\n" +
                "VALUES(%s,%f,'%s')",timestamp,action.getPrice(),action.getAction()));
    }

}
