package jahirfiquitiva.iconshowcase.adapters;

import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pitchedapps.butler.iconrequest.App;
import com.pitchedapps.butler.iconrequest.IconRequest;

import java.util.ArrayList;

import jahirfiquitiva.iconshowcase.R;
import jahirfiquitiva.iconshowcase.config.Config;
import jahirfiquitiva.iconshowcase.holders.RequestHolder;
import jahirfiquitiva.iconshowcase.utilities.Preferences;

public class RequestsAdapter extends RecyclerView.Adapter<RequestHolder> {

    private OnItemsChanged onItemsChanged = null;

    public RequestsAdapter(OnItemsChanged onItemsChanged) {
        this.onItemsChanged = onItemsChanged;
    }

    @Nullable
    private ArrayList<App> getApps() {
        return IconRequest.get() != null ? IconRequest.get().getApps() : null;
    }

    @Nullable
    public ArrayList<App> getSelectedApps() {
        return IconRequest.get() != null ? IconRequest.get().getSelectedApps() : null;
    }

    @Override
    public RequestHolder onCreateViewHolder(ViewGroup parent, int position) {
        Preferences mPrefs = new Preferences(parent.getContext());
        View view = LayoutInflater.from(parent.getContext()).inflate((Config.get().devOptions() ?
                mPrefs.getDevListsCards() : Config.get().bool(R.bool.request_cards)) ?
                R.layout.card_app_to_request : R.layout.item_app_to_request, parent, false);
        return new RequestHolder(view, new RequestHolder.OnAppClickListener() {
            @Override
            public void onClick(AppCompatCheckBox checkBox, App item) {
                onItemClick(checkBox, item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return getApps() != null ? getApps().size() : 0;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public void onBindViewHolder(RequestHolder holder, int position) {
        //noinspection ConstantConditions
        holder.setItem(getApps().get(holder.getAdapterPosition()));
    }

    public void selectOrDeselectAll() {
        final IconRequest ir = IconRequest.get();
        if (ir != null) {
            if (getSelectedApps() != null && getSelectedApps().isEmpty()) {
                ir.selectAllApps();
            } else {
                ir.unselectAllApps();
            }
            notifyDataSetChanged();
            if (onItemsChanged != null)
                onItemsChanged.doOnItemsChanged();
        }
    }

    private void onItemClick(AppCompatCheckBox checkBox, App app) {
        final IconRequest ir = IconRequest.get();
        if (ir != null && ir.getApps() != null) {
            ir.toggleAppSelected(app);
            checkBox.setChecked(ir.isAppSelected(app));
            if (onItemsChanged != null)
                onItemsChanged.doOnItemsChanged();
        }
    }

    public interface OnItemsChanged {
        void doOnItemsChanged();
    }

}