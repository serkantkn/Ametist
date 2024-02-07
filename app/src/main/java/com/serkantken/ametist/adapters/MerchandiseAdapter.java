package com.serkantken.ametist.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.serkantken.ametist.R;
import com.serkantken.ametist.databinding.MerchandiseListLayoutBinding;
import com.serkantken.ametist.models.MerchandiseModel;
import com.serkantken.ametist.utilities.Utilities;

import java.util.ArrayList;

import eightbitlab.com.blurview.BlurView;

public class MerchandiseAdapter extends RecyclerView.Adapter<MerchandiseAdapter.MerchandiseViewHolder>
{
    private Context context;
    private Utilities utilities;
    private ArrayList<MerchandiseModel> merchandiseList;

    public MerchandiseAdapter(Context context, ArrayList<MerchandiseModel> merchandiseList, Utilities utilities) {
        this.context = context;
        this.merchandiseList = merchandiseList;
        this.utilities = utilities;
    }

    @NonNull
    @Override
    public MerchandiseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MerchandiseViewHolder(MerchandiseListLayoutBinding.inflate(
                LayoutInflater.from(context),
                parent,
                false
        ));
    }

    @Override
    public void onBindViewHolder(@NonNull MerchandiseViewHolder holder, int position) {
        MerchandiseModel merchandiseModel = merchandiseList.get(position);
        utilities.blur(new BlurView[]{holder.binding.blur}, 10f, false);
        holder.binding.name.setText(merchandiseModel.getName());
        holder.binding.price.setText(merchandiseModel.getPrice());
        Glide.with(context)
                .load(merchandiseModel.getPhoto())
                .placeholder(AppCompatResources.getDrawable(context, R.drawable.ametist_cover_default_mdpi))
                .into(holder.binding.merchandiseImage);
    }

    @Override
    public int getItemCount() {
        if (merchandiseList.isEmpty())
            return 0;
        else
            return merchandiseList.size();
    }

    public class MerchandiseViewHolder extends RecyclerView.ViewHolder {
        MerchandiseListLayoutBinding binding;
        public MerchandiseViewHolder(MerchandiseListLayoutBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;
        }
    }
}
