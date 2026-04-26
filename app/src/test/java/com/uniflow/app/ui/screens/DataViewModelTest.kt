package com.uniflow.app.ui.screens

import com.uniflow.app.data.repository.DataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock

@OptIn(ExperimentalCoroutinesApi::class)
class DataViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private var dataRepository: DataRepository = mock(DataRepository::class.java)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        dataRepository = mock(DataRepository::class.java)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `clearPreview sets state to Idle`() = runTest {
        val viewModel = DataViewModel(dataRepository)
        viewModel.clearPreview()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is DataUiState.Idle)
        assertTrue(viewModel.importedCourses.value.isEmpty())
        assertTrue(viewModel.importedLecturers.value.isEmpty())
    }
}
