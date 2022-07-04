package com.example.memorygame

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.memorygame.models.BoardSize
import com.example.memorygame.models.MemoryCard
import com.squareup.picasso.Picasso
import kotlin.math.min

class MemoyrBoardAdapter(
    private val context: Context,
    private val boardSize: BoardSize,
    private val cards: List<MemoryCard>,
    private val cardClickListener:CardClickListner
) :
    RecyclerView.Adapter<MemoyrBoardAdapter.ViewHolder>() {

    companion object{
        private const val MARGIN_SIZE=10 // it is similar to the static method
        private const val TAG="MemoyrBoardAdapter"
    }
    interface CardClickListner{
        fun onCardClicked(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val cardwidth=parent.width/boardSize.getWidth()-(2* MARGIN_SIZE)
        val cardHeight=parent.height/boardSize.getHeight()-(2*MARGIN_SIZE)
        val cardSideLenght= min(cardwidth,cardHeight)

    val view:View= LayoutInflater.from(context).inflate(R.layout.memory_card,parent,false)
        val layoutParams = view.findViewById<CardView>(R.id.CardView).layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.width=cardSideLenght
        layoutParams.height=cardSideLenght
        layoutParams.setMargins(MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE)

        return ViewHolder(view)
    }

    override fun getItemCount() = boardSize.numCards

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }




    inner class ViewHolder(itemView:View) :RecyclerView.ViewHolder(itemView) {
        private val imageButton=itemView.findViewById<ImageButton>(R.id.imageButton)
        fun bind(position: Int) {
            val memoryCard=cards[position]
            if(memoryCard.isFaceUp){
                if(memoryCard.imageUri!=null){
                    Picasso.get().load(memoryCard.imageUri).placeholder(R.drawable.ic_image).into(imageButton)
                }
                else{
                    imageButton.setImageResource(memoryCard.identifier)
                }
            }else{

                imageButton.setImageResource(  R.drawable.ic_launcher_background)  // here we can change our image of memory
            }



            imageButton.alpha = if(memoryCard.isMatched) .4f else 1.0f

            val colorStateList = if(memoryCard.isMatched) ContextCompat.getColorStateList(context,R.color.color_gray) else null
            ViewCompat.setBackgroundTintList(imageButton,colorStateList)


            imageButton.setOnClickListener{
               Log.i(TAG, "Clicked on position $position")
                cardClickListener.onCardClicked(position)
            }
            //no-op
        }
    }

}
