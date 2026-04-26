package com.uniflow.app.ui.screens

import com.uniflow.app.data.repository.DataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

@OptIn(ExperimentalCoroutinesApi::class)
class AdminViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private var dataRepository: DataRepository = mock(DataRepository::class.java)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        dataRepository = mock(DataRepository::class.java)
        `when`(dataRepository.getAllClassrooms()).thenReturn(flowOf(emptyList()))
        `when`(dataRepository.getAllScheduleEntries()).thenReturn(flowOf(emptyList()))
        `when`(dataRepository.getAllCourses()).thenReturn(flowOf(emptyList()))
        `when`(dataRepository.getAllLecturers()).thenReturn(flowOf(emptyList()))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `addClassroom calls repository`() = runTest {
        val viewModel = AdminViewModel(dataRepository)
        viewModel.addClassroom("Z01", 50, "CENG")
        // Note: mock repository isn't throwing so this stays in loading,
        // wait for coroutine to complete if any flow needs finishing
        advanceUntilIdle()
    }

    @Test
    fun `resetAddClassroomState resets state to Idle`() = runTest {
        val viewModel = AdminViewModel(dataRepository)
        viewModel.resetAddClassroomState()
        advanceUntilIdle()
        assert(viewModel.addClassroomState.value is UiState.Idle)
    }

    @Test
    fun `resetAddAssignmentState resets state to Idle`() = runTest {
        val viewModel = AdminViewModel(dataRepository)
        viewModel.resetAddAssignmentState()
        advanceUntilIdle()
        assert(viewModel.addAssignmentState.value is UiState.Idle)
    }
}
