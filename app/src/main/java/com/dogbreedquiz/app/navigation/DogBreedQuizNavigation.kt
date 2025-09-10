package com.dogbreedquiz.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dogbreedquiz.app.ui.screens.achievements.AchievementsScreen
import com.dogbreedquiz.app.ui.screens.feedback.CorrectAnswerScreen
import com.dogbreedquiz.app.ui.screens.feedback.IncorrectAnswerScreen
import com.dogbreedquiz.app.ui.screens.home.HomeScreen
import com.dogbreedquiz.app.ui.screens.onboarding.WelcomeScreen
import com.dogbreedquiz.app.ui.screens.onboarding.TutorialScreen
import com.dogbreedquiz.app.ui.screens.progress.ProgressScreen
import com.dogbreedquiz.app.ui.screens.quiz.QuizScreen
import com.dogbreedquiz.app.ui.screens.quiz.QuizCompleteScreen
import com.dogbreedquiz.app.ui.screens.settings.SettingsScreen

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object Tutorial : Screen("tutorial/{step}") {
        fun createRoute(step: Int) = "tutorial/$step"
    }
    object Home : Screen("home")
    object Quiz : Screen("quiz")
    object CorrectAnswer : Screen("correct_answer/{questionId}") {
        fun createRoute(questionId: String) = "correct_answer/$questionId"
    }
    object IncorrectAnswer : Screen("incorrect_answer/{questionId}") {
        fun createRoute(questionId: String) = "incorrect_answer/$questionId"
    }
    object QuizComplete : Screen("quiz_complete")
    object Progress : Screen("progress")
    object Achievements : Screen("achievements")
    object Settings : Screen("settings")
}

@Composable
fun DogBreedQuizNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Welcome.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onGetStarted = {
                    navController.navigate(Screen.Tutorial.createRoute(1)) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                },
                onLearnMore = {
                    // Navigate to tutorial or info screen
                    navController.navigate(Screen.Tutorial.createRoute(1))
                }
            )
        }
        
        composable(Screen.Tutorial.route) { backStackEntry ->
            val step = backStackEntry.arguments?.getString("step")?.toIntOrNull() ?: 1
            TutorialScreen(
                step = step,
                onNext = { nextStep ->
                    if (nextStep <= 3) {
                        navController.navigate(Screen.Tutorial.createRoute(nextStep))
                    } else {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Welcome.route) { inclusive = true }
                        }
                    }
                },
                onBack = {
                    if (step > 1) {
                        navController.navigate(Screen.Tutorial.createRoute(step - 1))
                    } else {
                        navController.navigateUp()
                    }
                },
                onSkip = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Home.route) {
            HomeScreen(
                onStartQuiz = {
                    navController.navigate(Screen.Quiz.route)
                },
                onViewProgress = {
                    navController.navigate(Screen.Progress.route)
                },
                onViewAchievements = {
                    navController.navigate(Screen.Achievements.route)
                },
                onOpenSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }
        
        composable(Screen.Quiz.route) {
            QuizScreen(
                onAnswerCorrect = { questionId ->
                    navController.navigate(Screen.CorrectAnswer.createRoute(questionId))
                },
                onAnswerIncorrect = { questionId ->
                    navController.navigate(Screen.IncorrectAnswer.createRoute(questionId))
                },
                onQuizComplete = {
                    navController.navigate(Screen.QuizComplete.route) {
                        popUpTo(Screen.Quiz.route) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }
        
        composable(Screen.CorrectAnswer.route) { backStackEntry ->
            val questionId = backStackEntry.arguments?.getString("questionId") ?: ""
            CorrectAnswerScreen(
                questionId = questionId,
                onContinue = {
                    navController.navigateUp()
                }
            )
        }
        
        composable(Screen.IncorrectAnswer.route) { backStackEntry ->
            val questionId = backStackEntry.arguments?.getString("questionId") ?: ""
            IncorrectAnswerScreen(
                questionId = questionId,
                onContinue = {
                    navController.navigateUp()
                }
            )
        }
        
        composable(Screen.QuizComplete.route) {
            QuizCompleteScreen(
                onRetry = {
                    navController.navigate(Screen.Quiz.route) {
                        popUpTo(Screen.QuizComplete.route) { inclusive = true }
                    }
                },
                onMainMenu = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.QuizComplete.route) { inclusive = true }
                    }
                },
                onShare = {
                    // Handle sharing functionality
                }
            )
        }
        
        composable(Screen.Progress.route) {
            ProgressScreen(
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }
        
        composable(Screen.Achievements.route) {
            AchievementsScreen(
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }
    }
}