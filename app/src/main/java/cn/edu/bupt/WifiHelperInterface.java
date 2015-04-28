package cn.edu.bupt;

import java.util.ArrayList;

public interface WifiHelperInterface{
    public void processWifiHelperStatusChanged(Status status);
    public void processIpInUse(ArrayList<String> ipList);
    public void processForceOfflineResponse(boolean succeed);
    public void processUnknownError(String message);

    public enum Status{
        LOADING_INDEX_PAGE,
        TRY_TO_LOGIN,
        LOGIN_FAILED,
        TRY_TO_FETCH_IP
    }
}
