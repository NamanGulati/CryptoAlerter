package com.crypto.trader.service.Impl;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.crypto.trader.dao.PriceDataDao;
import com.crypto.trader.models.CryptopianResponse;
import com.crypto.trader.models.TradeAction;
import com.crypto.trader.service.TraderService;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import javax.inject.Named;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;


@Named
public class TraderServiceImpl implements TraderService {

    Logger logger = LoggerFactory.getLogger(TraderServiceImpl.class);

    @Value("${cryptopian.api.url}")
    public String apiUrl;

    @Value("${cryptopian.api.key}")
    public String key;

    @Value("${sns.trader.topicName}")
    public String snsTopicName;



    @Autowired
    PriceDataDao priceDataDao;



    RestTemplate template = new RestTemplate();

    @Override
    public void trade() {

        priceDataDao.deleteOldRecords();
        try {
            final double currentPrice = getPrice();
           List<Double> pricesHistory = priceDataDao.getPrices();
           if(pricesHistory.size()==0){
               writeCurrentIndexPrice(currentPrice);
           }
           else {
               pricesHistory.forEach(lastIndexPrice -> {
                   try {
                       TradeAction lastTrade = priceDataDao.getLastTrade();
                       logger.info("last traded at: "+ lastTrade);
                       logger.info("crypto current price: " + currentPrice + " comparing against: " + lastIndexPrice);
                       if (currentPrice > lastTrade.getPrice() && lastTrade.getAction().equals("BUY")) {
                           double diff = ((currentPrice - lastIndexPrice) / lastIndexPrice) * 100;
                           logger.info("diff: " + diff);
                           if (diff >= 5) {
                               krakenSell();
                               lastTrade.setPrice(currentPrice);
                               writeTrade(TradeAction.Builder().setPrice(currentPrice).setAction("SELL"));
                           }
                       }
                       if (currentPrice < lastTrade.getPrice() && lastTrade.getAction().equals("SELL")) {
                           double diff = ((lastIndexPrice - currentPrice) / currentPrice) * 100;
                           if (diff >= 5) {
                               krakenBuy();
                               lastTrade.setPrice(currentPrice);
                               lastTrade.setExecuted(true);
                               writeTrade(TradeAction.Builder().setPrice(currentPrice).setAction("BUY"));
                           }
                       }

                   } catch (Exception e) {
                       logger.error("Error: " + e);
                   }
               });
           }
           writeCurrentIndexPrice(currentPrice);
        } catch (Exception e){logger.error(e.getMessage());}
    }

    private double getPrice() throws URISyntaxException {
        URI uri = new URI(apiUrl);
        logger.info("api url: " + uri);
        HttpHeaders headers= new HttpHeaders();
        headers.add("Apikey",key);
        ResponseEntity<CryptopianResponse> response = template.exchange(uri,HttpMethod.GET,new HttpEntity<>(headers),CryptopianResponse.class);
        return response.getBody().USD;
    }



    private void krakenSell() {
        logger.info("Selling");
        logger.info("publishing SNS Message: "+SNSInterface.publishSNSMessage("Sell",snsTopicName));
    }
    private void krakenBuy(){
        logger.info("Selling");
        SNSInterface.publishSNSMessage("BUY",snsTopicName);
    }

    private void writeCurrentIndexPrice(double indexPrice){
        priceDataDao.putData(DateTime.now(DateTimeZone.getDefault()),indexPrice);
    }
    private void writeTrade(TradeAction action){
        priceDataDao.writeTrade(DateTime.now(DateTimeZone.getDefault()),action);
    }

}

class SNSInterface{

    private static AmazonSNS snsClient = AmazonSNSClient.builder().withRegion("us-east-1").build();

    public static String publishSNSMessage(String message, String topicARN){
        PublishRequest publishRequest = new PublishRequest(topicARN,message);
        PublishResult publishResult = snsClient.publish(publishRequest);
        return publishResult.getMessageId();
    }
}
