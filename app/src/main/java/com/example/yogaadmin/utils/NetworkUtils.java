package com.example.yogaadmin.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class NetworkUtils {
    /**
     * Check if network is available
     * @param context Application context
     * @return true if network is available, false otherwise
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager == null) {
            return false;
        }

        // For Android M and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network network = connectivityManager.getActiveNetwork();
            if (network == null) return false;

            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
            return capabilities != null && (
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
            );
        }
        // For older Android versions
        else {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
    }

    /**
     * Check if there is actual internet connection
     * @param context Application context
     * @return true if internet is available, false otherwise
     */
    public static boolean isInternetAvailable(Context context) {
        // First check network availability
        if (!isNetworkAvailable(context)) {
            return false;
        }

        // Additional check for internet connectivity
        try {
            // Attempt to connect to Google's DNS
            Socket socket = new Socket();
            SocketAddress sockaddr = new InetSocketAddress("8.8.8.8", 53);
            socket.connect(sockaddr, 1500);
            socket.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Get current network type
     * @param context Application context
     * @return Network type as a string
     */
    public static String getNetworkType(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager == null) {
            return "No Network";
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network network = connectivityManager.getActiveNetwork();
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);

            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    return "WIFI";
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    return "MOBILE";
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    return "ETHERNET";
                }
            }
        } else {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            if (activeNetworkInfo != null) {
                return activeNetworkInfo.getTypeName();
            }
        }

        return "No Network";
    }

    /**
     * Check network connection speed (basic estimation)
     * @param context Application context
     * @return Network speed category
     */
    public static String getNetworkSpeedCategory(Context context) {
        String networkType = getNetworkType(context);

        switch (networkType) {
            case "WIFI":
                return "High Speed";
            case "MOBILE":
                return "Medium Speed";
            case "ETHERNET":
                return "Very High Speed";
            default:
                return "No Connection";
        }
    }
}