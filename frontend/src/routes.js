import React from "react";
import {Navigate, Route, Routes} from 'react-router-dom'
import {
	AuthPage, DeliveryPage, MainMenuPage, OrdersPage,
	RegistrationPage,
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
			<Route path="/" element={<MainMenuPage/>}/>
			<Route path="/users" element={<UsersPage/>}/>
			<Route path="/my-orders" element={<OrdersPage/>}/>
			<Route path="/delivery" element={<DeliveryPage/>}/>
			<Route path="*" element={<Navigate to="/" replace/>}/>
		</Routes>
	)
}
