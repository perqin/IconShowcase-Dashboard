/*
 * Copyright (c) 2017 Jahir Fiquitiva
 *
 * Licensed under the CreativeCommons Attribution-ShareAlike 
 * 4.0 International License. You may not use this file except in compliance 
 * with the License. You may obtain a copy of the License at
 *
 *    http://creativecommons.org/licenses/by-sa/4.0/legalcode
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Special thanks to the project contributors and collaborators
 * 	https://github.com/jahirfiquitiva/IconShowcase#special-thanks
 */

package jahirfiquitiva.iconshowcase.tasks;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import jahirfiquitiva.iconshowcase.R;
import jahirfiquitiva.iconshowcase.models.WallpaperItem;
import jahirfiquitiva.iconshowcase.utilities.JSONParser;

public class DownloadJSONTask extends BasicTaskLoader<ArrayList<WallpaperItem>> {

    private InterestingConfigChanges mLastConfig = new InterestingConfigChanges();
    private ArrayList<WallpaperItem> wallpapers;

    public DownloadJSONTask(Context context) {
        super(context);
    }

    /**
     * This is where the bulk of our work is done.  This function is called in a background thread
     * and should generate a new set of data to be published by the loader.
     */
    @Override
    public ArrayList<WallpaperItem> loadInBackground() {
        ArrayList<WallpaperItem> wallpapers = new ArrayList<>();
        JSONObject json = JSONParser.getJSONFromURL(getContext(),
                getContext().getResources().getString(R.string.wallpapers_json_link));
        if (json != null) {
            try {
                JSONArray jsonWallpapers = json.getJSONArray("wallpapers");
                for (int j = 0; j < jsonWallpapers.length(); j++) {
                    JSONObject nWallpaper = jsonWallpapers.getJSONObject(j);
                    String copyright = "";
                    try {
                        copyright = nWallpaper.getString("copyright");
                    } catch (Exception ignored) {
                    }
                    String dimensions = "";
                    try {
                        dimensions = nWallpaper.getString("dimensions");
                    } catch (Exception ignored) {
                    }
                    String thumbnail = null;
                    try {
                        thumbnail = nWallpaper.getString("thumbnail");
                    } catch (Exception ignored) {
                    }
                    if (thumbnail == null) thumbnail = "null";
                    boolean downloadable = true;
                    try {
                        downloadable = nWallpaper.getString("downloadable").equals("true");
                    } catch (Exception ignored) {
                    }
                    wallpapers.add(new WallpaperItem(nWallpaper.getString("name"),
                            nWallpaper.getString("author"), nWallpaper.getString("url"),
                            thumbnail, dimensions, copyright, downloadable));
                }
                return wallpapers;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Called when there is new data to deliver to the client.  The super class will take
     * care of
     * delivering it; the implementation here just adds a little more logic.
     */
    @Override
    public void deliverResult(ArrayList<WallpaperItem> apps) {
        if (isReset()) {
            // An async query came in while the loader is stopped.  We
            // don't need the result.
            if (apps != null) {
                onReleaseResources(apps);
            }
        }
        ArrayList<WallpaperItem> oldApps = wallpapers;
        wallpapers = apps;

        if (isStarted()) {
            // If the Loader is currently started, we can immediately
            // deliver its results.
            super.deliverResult(apps);
        }

        // At this point we can release the resources associated with
        // 'oldApps' if needed; now that the new result is delivered we
        // know that it is no longer in use.
        if (oldApps != null) {
            onReleaseResources(oldApps);
        }
    }

    /**
     * Handles a request to start the Loader.
     */
    @Override
    protected void onStartLoading() {
        if (wallpapers != null) {
            // If we currently have a result available, deliver it
            // immediately.
            deliverResult(wallpapers);
        }

        // Has something interesting in the configuration changed since we
        // last built the app list?
        boolean configChange = mLastConfig.applyNewConfig(getContext().getResources());

        if (takeContentChanged() || wallpapers == null || configChange) {
            // If the data has changed since the last time it was loaded
            // or is not currently available, start a load.
            forceLoad();
        }
    }

    /**
     * Handles a request to stop the Loader.
     */
    @Override
    protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    /**
     * Handles a request to cancel a load.
     */
    @Override
    public void onCanceled(ArrayList<WallpaperItem> apps) {
        super.onCanceled(apps);

        // At this point we can release the resources associated with 'apps'
        // if needed.
        onReleaseResources(apps);
    }

    /**
     * Handles a request to completely reset the Loader.
     */
    @Override
    protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();

        // At this point we can release the resources associated with 'apps'
        // if needed.
        if (wallpapers != null) {
            onReleaseResources(wallpapers);
            wallpapers = null;
        }
    }

    /**
     * Helper function to take care of releasing resources associated with an actively loaded data
     * set.
     */
    protected void onReleaseResources(ArrayList<WallpaperItem> apps) {
        // For a simple List<> there is nothing to do.  For something
        // like a Cursor, we would close it here.
    }

    public interface JSONDownloadCallback {
        void onPreExecute(Context context);

        void onSuccess(ArrayList<WallpaperItem> wallpapers);
    }
}