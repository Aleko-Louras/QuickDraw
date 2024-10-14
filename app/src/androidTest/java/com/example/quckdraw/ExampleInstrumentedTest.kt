package com.example.quckdraw

import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.lifecycle.testing.TestLifecycleOwner
import junit.framework.TestCase
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule

@RunWith(AndroidJUnit4::class)
class DrawingViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: DrawingViewModel

    @Before
    fun setUp() {
        viewModel = DrawingViewModel()
    }

    @Test
    fun testInitialPenValues() {
        val initialPen = viewModel.penLiveData.value
        assertNotNull(initialPen)
        assertEquals(Color.BLUE, initialPen?.color)
        assertEquals(10f, initialPen?.size)
        assertEquals(Shape.LINE, initialPen?.shape)
    }

    @Test
    fun testSetPenColor() {
        val observer = Observer<Pen> {}
        try {
            viewModel.penLiveData.observeForever(observer)
            viewModel.setPenColor(Color.RED)
            val updatedPen = viewModel.penLiveData.value
            assertEquals(Color.RED, updatedPen?.color)
        } finally {
            viewModel.penLiveData.removeObserver(observer)
        }
    }

    @Test
    fun testSetPenSize() {
        val observer = Observer<Pen> {}
        try {
            viewModel.penLiveData.observeForever(observer)
            viewModel.setPenSize(20f)
            val updatedPen = viewModel.penLiveData.value
            assertEquals(20f, updatedPen?.size)
        } finally {
            viewModel.penLiveData.removeObserver(observer)
        }
    }

    @Test
    fun testSetPenShape() {
        val observer = Observer<Pen> {}
        try {
            viewModel.penLiveData.observeForever(observer)
            viewModel.setPenShape(Shape.CIRCLE)
            val updatedPen = viewModel.penLiveData.value
            assertEquals(Shape.CIRCLE, updatedPen?.shape)
        } finally {
            viewModel.penLiveData.removeObserver(observer)
        }
    }

    @Test
    fun testStartPathAndAddToPath() {
        assertEquals(0, viewModel.getNumberOfPaths())

        viewModel.startPath(100f, 100f)
        assertEquals(1, viewModel.getNumberOfPaths())

        viewModel.addToPath(200f, 200f)
        assertEquals(1, viewModel.getNumberOfPaths())
    }

    @Test
    fun testIsLine() {
        // Default shape is LINE
        assertTrue(viewModel.isLine())

        viewModel.setPenShape(Shape.CIRCLE)
        assertFalse(viewModel.isLine())

        viewModel.setPenShape(Shape.LINE)
        assertTrue(viewModel.isLine())
    }


    @Test
    fun testGetBitmap() {
        val bitmap = viewModel.getBitmap()
        assertNotNull(bitmap)
        assertEquals(800, bitmap.width)
        assertEquals(800, bitmap.height)
    }
    private val vm = DrawingViewModel()
    private val lifecycleOwner = TestLifecycleOwner()
    @Test
    fun doesPenColorChange() {
        val before = vm.penLiveData.value?.color
        var callbackFired = false
        vm.penLiveData.observe(lifecycleOwner){
            callbackFired = true
        }
        vm.setPenColor(10)
        TestCase.assertTrue(callbackFired)
        TestCase.assertNotSame(before, vm.penLiveData.value?.color)
    }

    @Test
    fun doesPenSizeChange() {
        val before = vm.penLiveData.value?.size
        var callbackFired = false
        vm.penLiveData.observe(lifecycleOwner){
            callbackFired = true
        }
        vm.setPenSize(1.0f)
        TestCase.assertTrue(callbackFired)
        TestCase.assertNotSame(before, vm.penLiveData.value?.size)
    }

    @Test
    fun pathsUpdatedAccordingly() {
        val before = vm.getNumberOfPaths()
        vm.addToPath(1.0f, 1.0f)
        TestCase.assertNotSame(before, vm.getNumberOfPaths())
    }
    @Test
    fun penShapeDidChange(){
        val before = vm.penLiveData.value?.shape
        var callbackFired = false
        vm.penLiveData.observe(lifecycleOwner){
            callbackFired = true
        }
        vm.setPenShape(Shape.SQUARE)
        TestCase.assertTrue(callbackFired)
        TestCase.assertNotSame(before, vm.penLiveData.value?.shape)
        assertSame(Shape.SQUARE, vm.penLiveData.value?.shape)
    }
}
