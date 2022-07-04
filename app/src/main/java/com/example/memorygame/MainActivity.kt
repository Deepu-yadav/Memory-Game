package com.example.memorygame

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.vectordrawable.graphics.drawable.ArgbEvaluator
import com.example.memorygame.models.BoardSize
import com.example.memorygame.models.MemoryGame
import com.example.memorygame.models.UserImageList
import com.example.memorygame.utils.EXTRA_BOARD_SIZE
import com.example.memorygame.utils.EXTRA_GAME_NAME
import com.github.jinatonic.confetti.CommonConfetti
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
        private const val CREATE_REQUEST_CODE=248
    }

    private lateinit var clRoot: CoordinatorLayout
    private lateinit var rvBoard: RecyclerView
    private lateinit var tvNumMoves: TextView
    private lateinit var tvNumPairs: TextView
    private lateinit var memoryGame: MemoryGame
    private lateinit var adapter: MemoyrBoardAdapter
    private val db=Firebase.firestore
    private var gameName:String?=null
    private var customGameImages:List<String>? = null


    private var boardSize: BoardSize = BoardSize.EASY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        clRoot = findViewById(R.id.clRoot)
        rvBoard = findViewById(R.id.rvBoard)
        tvNumMoves = findViewById(R.id.tvNumMoves)
        tvNumPairs = findViewById(R.id.tvNumPairs)

        setupBoard()

    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.memu_main,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){

            R.id.mi_refresh ->{
                if(memoryGame.getNumMoves() > 0 && !memoryGame.haveWonGame()){
                    showAlertDialog("Quit your current Game ?",null,View.OnClickListener {
                     setupBoard()
                    })
                }else{
                    setupBoard()
                }
                return true
            }

            R.id.mi_new_size ->{
                showNewDialog()
                return true
            }
            R.id.mi_custom ->{
                showCreationDialog()
                return true
            }
            R.id.mi_download ->{
                showDownloadDialog()
                return true
            }

        }
        return super.onOptionsItemSelected(item)
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode== CREATE_REQUEST_CODE && resultCode== Activity.RESULT_OK){
            val customGameName=data?.getStringExtra(EXTRA_GAME_NAME)
            if(customGameName==null){
                Log.e(TAG, "Got null custom game from CreateActivity")
                return
            }
            downloadGame(customGameName)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun showDownloadDialog() {
val boarddownloadView = LayoutInflater.from(this).inflate(R.layout.dialog_download_board,null)
        showAlertDialog("Fetch memory game",boarddownloadView,View.OnClickListener {
            // Grab the text game name that the user wants to download
            val etDownloadGame=boarddownloadView.findViewById<EditText>(R.id.etDownloadGame)
            val gameToDownload=etDownloadGame.text.toString().trim()
            downloadGame(gameToDownload)
        })
    }

    private fun downloadGame(customGameName: String) {
      db.collection("games").document(customGameName).get().addOnSuccessListener { document ->

       val userImageList=  document.toObject(UserImageList::class.java)
          if(userImageList?.images==null){
              Log.e(TAG, "Invalid custom game data from Firestore" )
              Snackbar.make(clRoot,"Sorry we couldn't find any such game , '$customGameName'",Snackbar.LENGTH_LONG).show()
              return@addOnSuccessListener
          }
          val numCards=userImageList.images.size * 2
          boardSize =BoardSize.getByValue(numCards)
          customGameImages=userImageList.images

          for(imageUrl in userImageList.images){
           Picasso.get().load(imageUrl).fetch()
          }
          Snackbar.make(clRoot,"You're now playing '$customGameName'!",Snackbar.LENGTH_LONG).show()
          gameName=customGameName
          setupBoard()


      } .addOnFailureListener{ exception ->
          Log.e(TAG, "Exception when reteriving game ",exception )

      }

    }


    private fun showCreationDialog() {
        val boardSizeView=LayoutInflater.from(this).inflate(R.layout.dialog_board_size,null)
        val radioGroupSize=boardSizeView.findViewById<RadioGroup>(R.id.radioGroup)
        showAlertDialog("Create your own memory board ",boardSizeView,View.OnClickListener {
            // set a new value for the a board size

            val desiredBoardSize=when(radioGroupSize.checkedRadioButtonId){
                R.id.rdEasy -> BoardSize.EASY
                R.id.rdMedium -> BoardSize.MEDIUM
                else ->  BoardSize.HARD
            }
            // navigate to a new activity
            val intent= Intent(this, CreateActivity::class.java)
            intent.putExtra(EXTRA_BOARD_SIZE,desiredBoardSize)
            startActivityForResult(intent, CREATE_REQUEST_CODE)

        })
    }

    private fun showNewDialog() {
        val boardSizeView=LayoutInflater.from(this).inflate(R.layout.dialog_board_size,null)
        val radioGroupSize=boardSizeView.findViewById<RadioGroup>(R.id.radioGroup)
        when(boardSize){
            BoardSize.EASY -> radioGroupSize.check(R.id.rdEasy)
            BoardSize.MEDIUM -> radioGroupSize.check(R.id.rdMedium)
            BoardSize.HARD -> radioGroupSize.check(R.id.rdHard)
        }

        showAlertDialog("Choose new size ",boardSizeView,View.OnClickListener {
         // set a new value for the a board size
            boardSize=when(radioGroupSize.checkedRadioButtonId){
                R.id.rdEasy -> BoardSize.EASY
                R.id.rdMedium -> BoardSize.MEDIUM
                else ->  BoardSize.HARD
            }
            gameName=null
            customGameImages=null
            setupBoard()
        })
    }

    private fun showAlertDialog(title:String,view: View?,positiveButtonClickListener: View.OnClickListener) {
       AlertDialog.Builder(this)
           .setTitle(title)
           .setView(view)
           .setNegativeButton("Cancel",null)
           .setPositiveButton("OK"){_, _ ->
               positiveButtonClickListener.onClick(null)

           }.show()

    }

    private fun setupBoard() {
        supportActionBar?.title=gameName?:getString(R.string.app_name)
        when(boardSize){
            BoardSize.EASY ->{
                tvNumMoves.text="Easy: 4 x 2"
                tvNumPairs.text="Pairs: 0 / 4"
            }
            BoardSize.MEDIUM -> {
                tvNumMoves.text="Medium: 6 x 3"
                tvNumPairs.text="Pairs: 0 / 9"
            }
            BoardSize.HARD -> {
                tvNumMoves.text="Hard: 6 x 4"
                tvNumPairs.text="Pairs: 0 / 12"
            }
        }

        tvNumPairs.setTextColor(ContextCompat.getColor(this, R.color.color_progress_none))
        memoryGame = MemoryGame(boardSize,customGameImages)

        adapter = MemoyrBoardAdapter(
            this,
            boardSize,
            memoryGame.cards,
            object : MemoyrBoardAdapter.CardClickListner {
                override fun onCardClicked(position: Int) {
                    updateGameWithFlip(position)
                }

            })
        rvBoard.adapter = adapter
        rvBoard.setHasFixedSize(true)
        rvBoard.layoutManager = GridLayoutManager(this, boardSize.getWidth())
    }



    private fun updateGameWithFlip(position: Int) {
        // Error checking
        if (memoryGame.haveWonGame()) {
            // alert to the user
            Snackbar.make(clRoot, "You already won!", Snackbar.LENGTH_LONG).show()
            return
        }
        if (memoryGame.isCardFaceUp(position)) {
            // alert the user of an invalid move
            Snackbar.make(clRoot, "Invalid move", Snackbar.LENGTH_SHORT).show()

            return

        }
        // Actually flip over the card
        if (memoryGame.flipCard(position)) {
            Log.i(TAG, "Found a match! Num pairs found:${memoryGame.numPairsFound} ")
            val color =  ArgbEvaluator().evaluate(
                memoryGame.numPairsFound.toFloat() / boardSize.getNumPairs(),
                ContextCompat.getColor(this,R.color.color_progress_none),
                ContextCompat.getColor(this,R.color.color_progress_full)


            ) as Int
            tvNumPairs.setTextColor(color)

            tvNumPairs.text = "Pairs:${memoryGame.numPairsFound} / ${boardSize.getNumPairs()}"
           if(memoryGame.haveWonGame()){
               Snackbar.make(clRoot,"You won! Congratulations.",Snackbar.LENGTH_LONG).show()
               CommonConfetti.rainingConfetti(clRoot, intArrayOf(Color.YELLOW,Color.GREEN,Color.MAGENTA)).oneShot()
           }
        }

        tvNumMoves.text = "Moves: ${memoryGame.getNumMoves()}"
        adapter.notifyDataSetChanged()
    }
}