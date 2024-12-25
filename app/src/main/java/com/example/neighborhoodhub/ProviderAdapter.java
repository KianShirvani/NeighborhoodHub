package com.example.neighborhoodhub;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

public class ProviderAdapter extends RecyclerView.Adapter<ProviderAdapter.ViewHolder> {
    private List<Provider> providers;

    public ProviderAdapter(List<Provider> providers) {
        this.providers = providers;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_provider, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Provider provider = providers.get(position);
        holder.nameText.setText(provider.getName());
        holder.phoneText.setText(provider.getPhone());
        holder.addressText.setText(provider.getAddress());
        holder.hoursText.setText(provider.getHours());

        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            new MaterialAlertDialogBuilder(context)
                    .setTitle(provider.getName())
                    .setItems(new String[]{"Call", "Find in Map"}, (dialog, which) -> {
                        if (which == 0) {
                            Intent intent = new Intent(Intent.ACTION_DIAL);
                            intent.setData(Uri.parse("tel:" + provider.getPhone()));
                            context.startActivity(intent);
                        } else {
                            String mapUrl = "https://www.google.com/maps/search/?api=1&query=" +
                                    Uri.encode(provider.getAddress());
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mapUrl));
                            context.startActivity(intent);
                        }
                    })
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return providers.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, phoneText, addressText, hoursText;

        ViewHolder(View view) {
            super(view);
            nameText = view.findViewById(R.id.providerName);
            phoneText = view.findViewById(R.id.providerPhone);
            addressText = view.findViewById(R.id.providerAddress);
            hoursText = view.findViewById(R.id.providerHours);
        }
    }
}