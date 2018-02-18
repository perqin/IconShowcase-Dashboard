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

package jahirfiquitiva.iconshowcase.holders.lists;

import android.support.annotation.NonNull;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import jahirfiquitiva.iconshowcase.events.OnLoadEvent;

/**
 * Created by Allan Wang on 2016-09-10.
 */
public abstract class ListHolderFrame<T> {

    private ArrayList<T> mList = new ArrayList<>();

    public abstract OnLoadEvent.Type getEventType();

    public void createList(@NonNull ArrayList<T> list) {
        mList = list;
        EventBus.getDefault().post(new OnLoadEvent(getEventType()));
    }

    public ArrayList<T> getList() {
        return mList;
    }

    public void clearList() {
        mList = null;
    }

    public boolean hasList() {
        return mList != null && !mList.isEmpty();
    }

    public boolean isEmpty() {
        return !hasList();
    }

    public boolean isNull() {
        return mList == null;
    }
}
