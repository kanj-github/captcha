package com.example.kanj.captcha.models

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.os.CountDownTimer
import com.example.kanj.captcha.api.CaptchData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.subjects.BehaviorSubject
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

class GameModel(val app: Application) : AndroidViewModel(app) {
    companion object {
        val TIME_PER_LEVEL = arrayListOf(10, 15, 20, 25, 30)
        val DEFAULT_LEVEL = 3
    }

    val captchas: ArrayList<ArrayList<CaptchData>>
    private val timePublisher = BehaviorSubject.create<Int>()
    private val imagePublisher = BehaviorSubject.create<String>()
    private val roundPublisher = BehaviorSubject.create<Int>()
    private val random = Random()
    private val solutions = ArrayList<SolutionData>()

    private var countdownTimer: CountDownTimer
    private var shownCaptcha: CaptchData
    private var remainingTime: Int = 0
    private var turns = 0
    var resultDisplayer: ResultDisplayer? = null

    init {
        captchas = arrayListOf(ArrayList(), ArrayList(), ArrayList(), ArrayList(), ArrayList())

        try {
            val inputStream = app.assets.open("captcha.json")
            val size = inputStream.available()
            val byteArray = ByteArray(size)
            inputStream.read(byteArray)
            inputStream.close()
            val json = String(byteArray)
            val capList: List<CaptchData> = Gson().fromJson(json, object : TypeToken<List<CaptchData>>(){}.type)
            capList.forEach {
                captchas[it.difficulty - 1].add(it)
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
        }

        val selected = random.nextInt(captchas[DEFAULT_LEVEL - 1].size - 1)
        shownCaptcha = captchas[DEFAULT_LEVEL - 1][selected]
        captchas[DEFAULT_LEVEL - 1].removeAt(selected)

        roundPublisher.onNext(++turns)
        imagePublisher.onNext(shownCaptcha.name)

        countdownTimer = MyTimer(TIME_PER_LEVEL[DEFAULT_LEVEL - 1])
        countdownTimer.start()
    }

    fun getTimeObservable() = timePublisher

    fun getImageObservable() = imagePublisher

    fun getRoundCountObservable() = roundPublisher

    fun submitAnswer(ans: String) {
        val correct = ans == shownCaptcha.answer
        solutions.add(SolutionData(shownCaptcha.name, ans, correct, shownCaptcha.difficulty, remainingTime))
        goToNextQuestion (correct)
    }

    private fun goToNextQuestion(correct: Boolean) {
        var nextLevel = if (correct) {
            shownCaptcha.difficulty + 1
        } else {
            shownCaptcha.difficulty - 1
        }

        if (nextLevel == 0 || turns == 5) {
            gameOver()
        } else if (nextLevel > 5) {
            nextLevel = 5
            showNextQuestion(nextLevel)
        } else {
            showNextQuestion(nextLevel)
        }
    }

    private fun showNextQuestion(nextLevel: Int) {
        val selected = random.nextInt(captchas[nextLevel - 1].size - 1)
        shownCaptcha = captchas[nextLevel - 1][selected]
        captchas[nextLevel - 1].removeAt(selected)
        roundPublisher.onNext(++turns)
        imagePublisher.onNext(shownCaptcha.name)
        countdownTimer.cancel()
        countdownTimer = MyTimer(TIME_PER_LEVEL[nextLevel - 1])
        countdownTimer.start()
    }

    private fun gameOver() {
        var score = 0
        var rate = 0
        var totalTime = 0
        solutions.forEach({
            if (it.correct) {
                score++
            }
            rate += it.timeTaken
            totalTime += TIME_PER_LEVEL[it.level - 1]
        })
        resultDisplayer?.showResult(solutions, score, rate, totalTime)
    }

    inner class MyTimer(seconds: Int) : CountDownTimer(seconds.toLong() * 1000, 1000) {
        init {
            remainingTime = seconds
            timePublisher.onNext(remainingTime)
        }

        override fun onTick(millisUntilFinished: Long) {
            remainingTime--
            timePublisher.onNext(remainingTime)
        }

        override fun onFinish() {
            goToNextQuestion(false)
        }
    }

    override fun onCleared() {
        countdownTimer.cancel()
    }

    // TODO: Assume that answer is user submitted answer. Check later
    class SolutionData(val image: String, val answer: String, val correct: Boolean, val level: Int, val timeTaken: Int)

    interface ResultDisplayer {
        fun showResult(results: ArrayList<SolutionData>, score: Int, rate: Int, totalTime: Int)
    }
}