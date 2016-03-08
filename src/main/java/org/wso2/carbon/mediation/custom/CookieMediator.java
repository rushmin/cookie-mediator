package org.wso2.carbon.mediation.custom;

import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by rushmin on 1/29/16.
 */
public class CookieMediator extends AbstractMediator{

    public static final String SET_COOKIE_HEADER_NAME = "Set-Cookie";
    public static final String EXCESS_TRANSPORT_HEADERS = "EXCESS_TRANSPORT_HEADERS";
    public static final String COOKIE_HEADER = "Cookie";

    public boolean mediate(MessageContext messageContext) {

        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext)messageContext).getAxis2MessageContext();


        List<String> newCookies = new ArrayList<String>();

        // Incorporate the first set-cookie header.
        Map<String, String> headers = (Map) axis2MessageContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);

        String firstSetCookieHeader = headers.get(SET_COOKIE_HEADER_NAME);

        newCookies.add(extractCookie(firstSetCookieHeader));

        // Incorporate the excess set-cookie headers
        Map excessHeaders = (Map) axis2MessageContext.getProperty(EXCESS_TRANSPORT_HEADERS);

        List<String> excessSetCookieHeaders = (List<String>) excessHeaders.get(SET_COOKIE_HEADER_NAME);

        for(String excessSetCookieHeader : excessSetCookieHeaders){
            newCookies.add(extractCookie(excessSetCookieHeader));
        }

        String[] existingCookies = getExistingCookies(headers.get(COOKIE_HEADER));

        String effectiveCookieString = buildCookieHeader(existingCookies, newCookies);

        headers.put(COOKIE_HEADER, effectiveCookieString);

        // Remove Set-Cookie headers
        headers.remove(SET_COOKIE_HEADER_NAME);
        ((Map) axis2MessageContext.getProperty(EXCESS_TRANSPORT_HEADERS)).remove(SET_COOKIE_HEADER_NAME);

        axis2MessageContext.setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, headers);

        return true;
    }

    private String[] getExistingCookies(String cookie) {


        String[] existingCookies = new String[0];

        if(cookie != null && !cookie.isEmpty()){

            existingCookies = cookie.split(";");

            // Trim the cookies.
            for(int i = 0; i < existingCookies.length; i++){
                existingCookies[i] = existingCookies[i].trim();
            }

        }

        return existingCookies;

    }


    private String buildCookieHeader(String[] existingCookies, List<String> newCookies) {

        List<String> effectiveCookies = new ArrayList<String>(Arrays.asList(existingCookies));

        for(String cookie : newCookies){
            if(!effectiveCookies.contains(cookie)){
                effectiveCookies.add(cookie);
            }
        }

        StringBuilder effectiveCookie = new StringBuilder();

        for(String cookie : effectiveCookies){
            effectiveCookie.append(cookie).append("; ");
        }

        return effectiveCookie.toString();

    }

    private String extractCookie(String firstSetCookieHeader) {
        return firstSetCookieHeader.split(";")[0];
    }


}
