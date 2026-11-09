package com.example.gymmanagement

import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GymMemberAdapter(
    private val members: MutableList<GymMember>,
    private val onEditClick: (position: Int, member: GymMember) -> Unit,
    private val onDeleteClick: (position: Int, member: GymMember) -> Unit
) : RecyclerView.Adapter<GymMemberAdapter.MemberViewHolder>() {
// может быть утечка памяти inner class (не в этом случае, а вообще)
    inner class MemberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvItemName)
        val tvCategory: TextView = itemView.findViewById(R.id.tvItemCategory)
        val tvDuration: TextView = itemView.findViewById(R.id.tvItemDuration)
        val tvExtra: TextView = itemView.findViewById(R.id.tvItemExtra)
        val tvPrice: TextView = itemView.findViewById(R.id.tvItemPrice)

        init {
            // U2 и D2: Контекстное меню при долгом нажатии
            itemView.setOnCreateContextMenuListener { menu, _, _ ->
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val currentMember = members[position]
                    menu.setHeaderTitle(currentMember.fullName)

                    // Пункт U2: Редактировать
                    val editItem = menu.add(Menu.NONE, 1, 1, "Редактировать")
                    editItem.setOnMenuItemClickListener {
                        onEditClick(position, currentMember)
                        true
                    }

                    // Пункт D2: Удалить
                    val deleteItem = menu.add(Menu.NONE, 2, 2, "Удалить")
                    deleteItem.setOnMenuItemClickListener {
                        onDeleteClick(position, currentMember)
                        true
                    }
                }
            }
        }
    }

    // 1) создание пустой формы
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_gym_member, parent, false)
        return MemberViewHolder(view)
    }

    // 2) надписи на готовую карточку
    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        val member = members[position]
        holder.tvName.text = member.fullName
        holder.tvCategory.text = "Категория: ${member.category}"
        holder.tvDuration.text = "Срок: ${member.durationMonths} мес."
        holder.tvExtra.text = "Тел: ${member.phone}"
        holder.tvPrice.text = "${"%.2f".format(member.totalPrice)} ₽"

        holder.tvExtra.text = "Тел: ${member.phone}\nТренер: ${member.trainer}\nФилиал: ${member.branch}"

    }

    override fun getItemCount(): Int = members.size
}