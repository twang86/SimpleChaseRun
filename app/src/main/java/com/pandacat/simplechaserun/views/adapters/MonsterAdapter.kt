package com.pandacat.simplechaserun.views.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.pandacat.simplechaserun.R
import com.pandacat.simplechaserun.data.params.MonsterParam
import com.pandacat.simplechaserun.utils.UnitsUtil

class MonsterAdapter(val listener: Listener): Adapter<MonsterAdapter.MonsterViewHolder>() {
    private var monsterParams = arrayListOf<MonsterParam>()

    interface Listener{
        fun onMonsterClicked(index : Int)
        fun onMonsterLongClicked(index: Int)
    }
    class MonsterViewHolder(itemView: View) : ViewHolder(itemView)
    {
        fun onBind(param: MonsterParam)
        {
            itemView.findViewById<ImageView>(R.id.monsterImage).setImageResource(param.monsterType.getSimpleImageResId())
            itemView.findViewById<TextView>(R.id.monsterName).text = param.monsterType.getDisplayName(itemView.context)
            itemView.findViewById<TextView>(R.id.monsterStartText).text = param.getStartText(itemView.context)
            itemView.findViewById<TextView>(R.id.monsterDelayText).text = param.getHeadStart()
            itemView.findViewById<TextView>(R.id.monsterSpeedText).text = UnitsUtil.getSpeedText(param.speedKPH, itemView.context)
            itemView.findViewById<TextView>(R.id.monsterStaminaText).text = param.getStaminaText(itemView.context)
        }
    }

    fun updateMonsters(params: ArrayList<MonsterParam>)
    {
        monsterParams = params
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonsterViewHolder {
        return MonsterViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_monster_param, parent, false))
    }

    override fun getItemCount() = monsterParams.count()

    override fun onBindViewHolder(holder: MonsterViewHolder, position: Int) {
        holder.onBind(monsterParams[position])
        holder.itemView.setOnClickListener{
            listener.onMonsterClicked(holder.adapterPosition)
        }
        holder.itemView.setOnLongClickListener{
            listener.onMonsterLongClicked(holder.adapterPosition)
            true
        }
    }
}