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

export const RegistrationPage = () => {

	const navigate = useNavigate()
	const {request} = useHttp()
	const {login} = useAuthContext()

	const {handleSubmit, control, reset} = useForm({
		defaultValues: {
			username: '',
			email: '',
			password: ''
		}
	})

	const onSubmit = async data => {
		// console.log(data)
		request('/registration', 'POST', data, false).then((userData) => {
			toast.success('Заявка на регистрацию отправлена')
			navigate('/auth')
		}).catch(() => {})
	}

	return (
		<Box component="main" sx={{display: "flex", alignItems: "center", justifyContent: "center", height: '100vh', width: '100%'}}>
			<CssBaseline/>
			<Sheet
				sx={{
					width: 350,
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
				</div>
				<Box component="form" display="flex" flexDirection="column" gap={2} onSubmit={handleSubmit(onSubmit)}>
					<Controller
						name={"username"}
						required
						fullWidth
						control={control}
						render={({field: {onChange, value}}) => (
							<FormControl>
								<FormLabel>Имя</FormLabel>
								<Input
									name="username"
									onChange={onChange}
									value={value}
									placeholder="Username"
								/>
							</FormControl>
						)}
					/>
					<Controller
						name={"email"}
						required
						fullWidth
						control={control}
						render={({field: {onChange, value}}) => (
							<FormControl>
								<FormLabel>E-mail</FormLabel>
								<Input
									name="email"
									type="email"
									onChange={onChange}
									value={value}
									placeholder="johndoe@email.com"
								/>
							</FormControl>
						)}
					/>
					<Controller
						name={"password"}
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
					<Button variant="soft" sx={{mt: 1 }} type="submit">Пройти посвящение</Button>
				</Box>
			</Sheet>
		</Box>
	);
}
