package com.example.quckdraw

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
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
                onCreateNewDrawingClick = {},
                onSignOutUser = {},
                onSharedClick = {},
                onUploadClick = {}

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
                onDeleteClick = {},
                onUploadClick = {}
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

    @Test
    fun testDrawingItemWasDeleted(){
        composeTestRule.setContent {
            DrawingListView(
                drawings = listOf(
                    DrawingData("Drawing 1", "/path/to/drawing1", timestamp = Date()),
                    DrawingData("Drawing 2", "/path/to/drawing2", timestamp = Date())
                ),
                onDrawingClick = {},
                onDeleteClick = {},
                onUploadClick = {},
                onCreateNewDrawingClick = {},
                onSignOutUser = {},
                onSharedClick = {}
            )
        }
        composeTestRule.onAllNodesWithText("Delete")[0].performClick()
        composeTestRule.onNodeWithText("Mock Drawing").assertDoesNotExist()


    }
    @Test
    fun testUpload() {
        var isuploadClicked: DrawingData? = null
        val mockDrawing = DrawingData("drawing1", "/path/to/drawing1", timestamp = Date())

        composeTestRule.setContent {
            DrawingItem(
                drawing = mockDrawing,
                onClick = {},
                onDeleteClick = {},
                onUploadClick = { isuploadClicked = mockDrawing }
            )
        }

        composeTestRule.onNodeWithText("Upload").performClick()
        assert(isuploadClicked == mockDrawing)
    }
    @Test
    fun testSignOut() {
        var issignOutClicked = false

        composeTestRule.setContent {
            DrawingListView(
                drawings = emptyList(),
                onDrawingClick = {},
                onDeleteClick = {},
                onUploadClick = {},
                onCreateNewDrawingClick = {},
                onSignOutUser = { issignOutClicked = true },
                onSharedClick = {}
            )
        }

        composeTestRule.onNodeWithText("Sign Out").performClick()
        assert(issignOutClicked)
    }
    @Test
    fun testSharedDrawings() {
        var issharedClicked = false

        composeTestRule.setContent {
            DrawingListView(
                drawings = emptyList(),
                onDrawingClick = {},
                onDeleteClick = {},
                onUploadClick = {},
                onCreateNewDrawingClick = {},
                onSignOutUser = {},
                onSharedClick = { issharedClicked = true }
            )
        }
        composeTestRule.onNodeWithText("Shared drawings").performClick()
        assert(issharedClicked)
    }


}