package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.StorePreferencesManager
import com.example.database.AppDatabase
import com.example.navigation.GestaoAppRoot
import com.example.repository.StoreRepository
import com.example.ui.theme.GestaoStoreTheme
import com.example.viewmodel.StoreViewModelFactory
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MainNavigationRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var database: AppDatabase
    private lateinit var factory: StoreViewModelFactory

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val repository = StoreRepository(database, StorePreferencesManager(context))
        factory = StoreViewModelFactory(repository)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `bottom navigation switches across all five main sections and more subscreens`() {
        composeTestRule.setContent {
            GestaoStoreTheme {
                GestaoAppRoot(viewModelFactory = factory)
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("dashboard_screen").assertIsDisplayed()

        composeTestRule.onNodeWithTag("nav_tab_inventory").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("inventory_screen").assertIsDisplayed()

        composeTestRule.onNodeWithTag("nav_tab_sales").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("sales_screen").assertIsDisplayed()

        composeTestRule.onNodeWithTag("nav_tab_clients").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("clients_screen").assertIsDisplayed()

        composeTestRule.onNodeWithTag("nav_tab_more").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("more_list").assertIsDisplayed()

        composeTestRule.onNodeWithTag("more_orders_item").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("orders_screen").assertIsDisplayed()

        composeTestRule.onNodeWithTag("top_bar_back_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("more_finance_item").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("finance_screen").assertIsDisplayed()

        composeTestRule.onNodeWithTag("top_bar_back_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("more_list").performScrollToNode(hasTestTag("more_reports_item"))
        composeTestRule.onNodeWithTag("more_reports_item").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("reports_screen").assertIsDisplayed()

        composeTestRule.onNodeWithTag("top_bar_back_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("more_list").performScrollToNode(hasTestTag("more_settings_item"))
        composeTestRule.onNodeWithTag("more_settings_item").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("settings_screen").assertIsDisplayed()
    }
}
