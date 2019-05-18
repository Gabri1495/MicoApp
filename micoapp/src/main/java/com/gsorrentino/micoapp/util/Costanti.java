package com.gsorrentino.micoapp.util;

public final class Costanti {
    /*Database*/
    public static final String DB_NAME = "mico_app.db";
    public static final String INSERT = "insert";
    public static final String UPDATE = "update";
    public static final String DELETE = "delete";
    public static final String REMOVE_FINDS = "removeFinds";
    public static final String REMOVE_RECEIVED = "removeReceived";

    public static final String SHARED_PREFERENCES = "pref";

    /*Return codes from callbacks*/
    public static final int REQUEST_ACCESS_FINE_LOCATION_PERMISSIONS = 1;
    public static final int REQUEST_CHECK_LOCALIZATION_SETTINGS = 2;

    /*Notifications IDs*/
    public static final int PERMISSION_NOTIFICATION_ID = 1;

    /*Channels*/
    public static final String PERMISSION_CHANNEL_ID = "Permissions";

    /*Default values*/
    public static final double LAT_DEFAULT = 44.498955;
    public static final double LNG_DEFAULT = 11.327591;
    public static final float ZOOM_DEFAULT = 6f;

}
