// import {
// 	Box,
// 	Button,
// 	Card,
// 	CardActions,
// 	CardContent,
// 	Checkbox,
// 	Container,
// 	FormControlLabel,
// 	TextField,
// 	Typography
// } from "@mui/material";
import {useNavigate} from "react-router-dom";
import {Controller, useForm} from "react-hook-form";
import {useHttp} from "../../hooks/http.hook";
import {useAuthContext} from "../../context/AuthContext";
import toast from "react-hot-toast";
import {Box, Button, CssBaseline, FormControl, FormLabel, Input, Link, Sheet, Typography} from "@mui/joy";
import * as React from 'react';
import {useColorScheme} from '@mui/joy/styles';

export const AuthPage = () => {

	const navigate = useNavigate()
	const {request} = useHttp()
	const {login} = useAuthContext()

	const {handleSubmit, control, reset} = useForm({
		defaultValues: {
			login: '',
			pass: ''
		}
	})

	const onSubmit = async data => {
		console.log(data)
		request('/auth/login', 'POST', data, false).then((userData) => {
			login(userData)
		// 	login({
		// 		accessToken: "string",
		// 		accessToken: "string",
		// 		user: {
		// 			"id": 1,
		// 			"login": "test_login",
		// 			"name": "Максим",
		// 			"role": {
		// 				"id": 7,
		// 				"name": "Разработчик"
		// 			}
		// 		}
		// 	})
		})
	}

	return (
		<Box component="main" sx={{display: "flex", alignItems: "center", justifyContent: "center", height: '100vh', width: '100%'}}>
			<CssBaseline/>
			<Sheet
				sx={{
					width: 300,
					mx: 'auto', // margin left & right
					my: 4, // margin top & bottom
					py: 3, // padding top & bottom
					px: 2, // padding left & right
					display: 'flex',
					flexDirection: 'column',
					gap: 2,
					borderRadius: 'sm',
					boxShadow: 'md',
				}}
				// variant="outlined"
			>
				<div>
					<Typography level="h4" component="h1">
						<b>BABLOSIS</b>
					</Typography>
					<Typography level="body-sm">Войдите в аккаунт для продолжения</Typography>
				</div>
				<Box component="form" display="flex" flexDirection="column" gap={2} onSubmit={handleSubmit(onSubmit)}>
					<Controller
						name={"login"}
						required
						fullWidth
						control={control}
						render={({field: {onChange, value}}) => (
							<FormControl>
								<FormLabel>E-mail</FormLabel>
								<Input
									name="login"
									type="login"
									onChange={onChange}
									value={value}
									placeholder="johndoe@email.com"
								/>
							</FormControl>
						)}
					/>
					<Controller
						name={"pass"}
						required
						fullWidth
						control={control}
						render={({field: {onChange, value}}) => (
							<FormControl>
								<FormLabel>Пароль</FormLabel>
								<Input
									// html input attribute
									onChange={onChange}
									value={value}
									name="password"
									type="password"
									placeholder="password"
								/>
							</FormControl>
						)}
					/>
					<Button variant="soft" sx={{mt: 1 /* margin top */}} type="submit">Войти</Button>
				</Box>
			</Sheet>
		</Box>
	);
}
