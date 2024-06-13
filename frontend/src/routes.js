import React from "react";
import {Navigate, Route, Routes} from 'react-router-dom'
import {
	AuthPage,
	CataclysmPage,
	MachinePage,
	RegistrationPage,
	TasksManagementPage,
	TasksPage,
	UsersPage
} from "./pages";


export const useRoutes = (userData) => {

	if (!userData) {
		return (
			<Routes>
				<Route path="/auth" element={<AuthPage/>}/>
				<Route path="/sign-up" element={<RegistrationPage/>}/>
				<Route path="*" element={<Navigate to="/auth" replace/>}/>
			</Routes>
		)
	}

	return (
		<Routes>
			<Route path="/auth" element={<AuthPage/>}/>
			<Route path="/sign-up" element={<RegistrationPage/>}/>
			<Route path="*" element={<Navigate to="/auth" replace/>}/>
		</Routes>
	)
}