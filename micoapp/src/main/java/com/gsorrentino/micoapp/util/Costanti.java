package com.gsorrentino.micoapp.util;

public final class Costanti {
    /*Database*/
    public static final int DB_VERSION = 1;
    public static final String DB_NAME = "mico_app.db";
    public static final String INSERT = "insert";
    public static final String UPDATE = "update";
    public static final String DELETE = "delete";
    public static final String REMOVE_FINDS = "removeFinds";
    public static final String REMOVE_RECEIVED = "removeReceived";

    /*Shared Preferences*/
    public static final String SHARED_PREFERENCES = "pref";
    public static final String LAT = "lat";
    public static final String LNG = "lng";
    public static final String ZOOM = "zoom";
    public static final String HISTORY_RADIO_SELECTION = "historyRadioSelection";
    public static final String ARCHIVE_RADIO_SELECTION = "archiveRadioSelection";
    public static final String ARCHIVE_SEARCH_OPEN = "archiveSearchOpen";
    public static final String ENCYCLOPEDIA_SAVE_LINK = "encyclopediaSaveLink";
    public static final String ENCYCLOPEDIA_SAVED_LINK = "encyclopediaSavedLink";
    public static final String CURRENT_PATHS = "currentPaths";
    public static final String ADDED_PATHS = "addedPaths";
    public static final String INITIAL_PATHS = "initialPaths";
    public static final String CURRENT_KEY = "currentKey";

    /*Return codes from callbacks*/
    public static final int REQUEST_ACCESS_FINE_LOCATION_PERMISSIONS = 1;
    public static final int REQUEST_CHECK_LOCALIZATION_SETTINGS = 2;
    public static final int REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSIONS = 3;
    public static final int REQUEST_READ_FILE = 4;
    public static final int REQUEST_IMAGE_CAPTURE = 5;
    public static final int REQUEST_OPEN_IMAGE = 6;
    public static final int REQUEST_UPDATE_FIND = 7;

    /*Notifications IDs*/
    public static final int PERMISSION_LOCALIZATION_NOTIFICATION_ID = 1;
    public static final int PERMISSION_STORAGE_NOTIFICATION_ID = 2;

    /*Channels*/
    public static final String PERMISSION_CHANNEL_ID = "Permissions";

    /*Default values*/
    public static final double LAT_DEFAULT = 44.498955;
    public static final double LNG_DEFAULT = 11.327591;
    public static final float ZOOM_DEFAULT = 6f;

    /*Intent Extra string*/
    private static final String INTENT_BASE = "com.gsorrentino.micoapp";
    public static final String INTENT_LATLNG = INTENT_BASE + ".latlng";
    public static final String INTENT_FIND = INTENT_BASE + ".find";

    public static final String CREATE_MODE = "create";
    public static final String EDIT_MODE = "edit";
}
