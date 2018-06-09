package com.example.kanj.captcha

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.kanj.captcha.models.GameModel
import com.squareup.picasso.Picasso

class ResultsAdapter(val picasso: Picasso, val result: ArrayList<GameModel.SolutionData>)
    : RecyclerView.Adapter<ResultsAdapter.ResultHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_result, parent,false)
        return ResultHolder(v)
    }

    override fun getItemCount() = result.size

    override fun onBindViewHolder(holder: ResultHolder, position: Int) {
        picasso.load("file:///android_asset/" + result[position].image + ".png").into(holder.image)
        holder.ans.setText(result[position].answer)
        holder.status.setText(if (result[position].correct) {
            "Correct"
        } else {
            "Incorrect"
        })
    }

    inner class ResultHolder(v: View) : RecyclerView.ViewHolder(v) {
        val image: ImageView
        val ans: TextView
        val status: TextView

        init {
            image = v.findViewById(R.id.icon)
            ans = v.findViewById(R.id.tv_ans)
            status = v.findViewById(R.id.tv_status)
        }
    }
}