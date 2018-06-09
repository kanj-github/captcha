package com.example.kanj.captcha

import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.ViewModelProviders
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import com.example.kanj.captcha.models.GameModel
import com.squareup.picasso.Picasso
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), GameModel.ResultDisplayer {
    private lateinit var gameModel: GameModel
    private lateinit var picasso: Picasso
    private var roundObserver: Disposable? = null
    private var timeObserver: Disposable? = null
    private var imageObserver: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        gameModel = ViewModelProviders.of(this).get(GameModel::class.java)
        gameModel.resultDisplayer = this
        picasso = Picasso.get()

        roundObserver = gameModel.getRoundCountObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    tv_round.setText("Round " + it + "/5")
                }, {})

        timeObserver = gameModel.getTimeObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    tv_time.setText("00:" + it)
                }, {})
        imageObserver = gameModel.getImageObservable()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    picasso.load("file:///android_asset/" + it + ".png").into(imageView)
                }, {})

        bt_submit.setOnClickListener({
            val ans = et_answer.text.toString().trim()
            if (!TextUtils.isEmpty(ans)) {
                gameModel.submitAnswer(ans)
                et_answer.setText("")
            }
        })
    }

    override fun showResult(results: ArrayList<GameModel.SolutionData>, score: Int, rate: Int, totalTime: Int) {
        game_view.visibility = View.GONE
        result_view.visibility = View.VISIBLE

        val adapter = ResultsAdapter(picasso, results)
        list_result.layoutManager = LinearLayoutManager(this)
        list_result.adapter = adapter

        tv_score.text = "" + score + "/5"
        tv_rate.text = "" + rate + "/" + totalTime
    }

    override fun onDestroy() {
        super.onDestroy()
        roundObserver?.dispose()
        timeObserver?.dispose()
        imageObserver?.dispose()
    }
}
