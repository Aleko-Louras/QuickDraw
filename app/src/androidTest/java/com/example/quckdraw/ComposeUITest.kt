package com.example.quckdraw

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.*
import org.junit.Rule
import org.junit.Test
import java.util.Date

class ComposeUITest {

    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun testDrawingListDisplaysItemsAndButtons() {
        composeTestRule.setContent {
            DrawingListView(
                drawings = listOf(
                    DrawingData("Drawing 1", "/path/to/drawing1", timestamp = Date()),
                    DrawingData("Drawing 2", "/path/to/drawing2", timestamp = Date())
                ),
                onDrawingClick = {},
                onDeleteClick = {},
                onCreateNewDrawingClick = {}
            )
        }

        // Verify the displayed drawing items
        composeTestRule.onNodeWithText("Drawing 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Drawing 2").assertIsDisplayed()

        // Verify the "Create New Drawing" button is clickable
        composeTestRule.onNodeWithText("Create New Drawing")
            .assertExists()
            .assertHasClickAction()
            .performClick()
    }

    @Test
    fun testDrawingItemInteractions() {
        val mockDrawing = DrawingData("Mock Drawing", "/path/to/mock", timestamp = Date())

        composeTestRule.setContent {
            DrawingItem(
                drawing = mockDrawing,
                onClick = {},
                onDeleteClick = {}
            )
        }

        // Assert the drawing item is displayed
        composeTestRule.onNodeWithText("Mock Drawing").assertIsDisplayed()

        // Check the delete button exists and is clickable
        composeTestRule.onNodeWithText("Delete")
            .assertExists()
            .assertHasClickAction()
            .performClick()
    }


}