package com.brainboost.brainboost.auth.util;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Random;

import static java.util.Objects.nonNull;

public abstract class GenericUtil {

    public static int generateOtpCode(){
        Random random = new Random();
        return random.nextInt(999999);
    }

    public static String generateAlphaNumeric(int length) {
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789"
                + "abcdefghijklmnopqrstuvxyz";

        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {

            int index
                    = (int)(AlphaNumericString.length()
                    * Math.random());
            sb.append(AlphaNumericString
                    .charAt(index));
        }

        return sb.toString();
    }


    public static String randomNumber(int len){
        final String AB = "0123456789";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        }
        return sb.toString();
    }

    public  static String getIpAddress(HttpServletRequest request){
            String clientIp;

            String clientCFConnectingIp = request.getHeader("cf-connecting-ip");
            if (nonNull(clientCFConnectingIp)) {
                clientIp = clientCFConnectingIp;
            } else {
                clientIp = request.getRemoteAddr();
            }
            return clientIp;
    }

    public static String formatName(String name){
        String[] nameArray = name.split(" ");
        return String.join("_", nameArray).toUpperCase();
    }
}
