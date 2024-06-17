import {useEffect, useState} from "react";
import {useNavigate} from "react-router-dom";
import {useHttp} from "../../hooks/http.hook";
import {useAuthContext} from "../../context/AuthContext";
import {
	Avatar,
	Box,
	Button,
	Container,
	CssBaseline, DialogTitle,
	FormControl,
	FormLabel, IconButton,
	Input,
	Link,
	Modal, ModalDialog, Select,
	Sheet, Stack, Table, Textarea,
	Typography, Option
} from "@mui/joy";
import * as React from 'react';
import {Controller, useForm} from "react-hook-form";
import toast from "react-hot-toast";
import {ROLES} from "../../config";
import EditRoundedIcon from '@mui/icons-material/EditRounded';

export const UsersPage = () => {
	const [open, setOpen] = useState(false);
	const [selectedUserEmail, setSelectedUserEmail] = useState(null);
	const [users, setUsers] = useState([]);

	const navigate = useNavigate()
	const {request} = useHttp()
	const {userData} = useAuthContext()

	const {handleSubmit, control, reset, setValue} = useForm({
		defaultValues: {
			role: 'NONE',
		}
	})

	const onSubmit = async data => {
		const payload = {email: selectedUserEmail, ...data}
		request(`/set_role?id=${selectedUserEmail}`, 'POST', payload).then(() => {
			toast.success('Роль сохранена')
			handleClose()
		}).catch(() => {})
	}

	const handleClose = () => {
		setSelectedUserEmail(null)
		fetchUsers()
		setOpen(false)
		reset()
	}

	const handleOpen = async (user) => {
		setOpen(true)
		setSelectedUserEmail(user.email)
		setValue("role", user.role)
	}

	const fetchUsers = async () => {
		request('/all_user', 'POST', null, true).then((data) => {
			setUsers(data.person)
		})
	}

	useEffect(() => {
		fetchUsers()
	}, []);


	return (
		<Box component="main"
		     sx={{paddingTop: 5, height: '100vh', width: '100%'}}>
			<CssBaseline/>
			<Container>
				<Box
					sx={{
						display: 'flex',
						mb: 1,
						gap: 1,
						flexDirection: {xs: 'column', sm: 'row'},
						alignItems: {xs: 'start', sm: 'center'},
						flexWrap: 'wrap',
						justifyContent: 'space-between',
					}}
				>
					<Typography level="h2" component="h1" marginBottom={3}>
						Пользователи
					</Typography>

					{users.length > 0 && <Sheet
						className="OrderTableContainer"
						variant="outlined"
						sx={{
							display: {xs: 'none', sm: 'initial'},
							width: '100%',
							borderRadius: 'sm',
							flexShrink: 1,
							overflow: 'auto',
							minHeight: 0,
						}}
					>
						<Table
							aria-labelledby="tableTitle"
							stickyHeader
							hoverRow
							sx={{
								'--TableCell-headBackground': 'var(--joy-palette-background-level1)',
								'--Table-headerUnderlineThickness': '1px',
								'--TableRow-hoverBackground': 'var(--joy-palette-background-level1)',
								'--TableCell-paddingY': '4px',
								'--TableCell-paddingX': '8px',
							}}
						>
							<thead>
							<tr>
								<th style={{padding: '12px 12px'}}>#</th>
								<th style={{padding: '12px 12px'}}>Имя</th>
								<th style={{padding: '12px 12px'}}>Почта</th>
								<th style={{padding: '12px 12px'}}>Роль</th>
								<th style={{padding: '12px 12px'}}></th>
							</tr>
							</thead>
							<tbody>
							{users.map((user, key) => (
								<tr key={key}>
									<td>
										<Typography level="body-xs">{key + 1}</Typography>
									</td>
									<td>
										{/*<Box sx={{display: 'flex', gap: 1, alignItems: 'center'}}>*/}
										{/*	<Avatar size="sm">{user.name.charAt(0)}</Avatar>*/}
										{/*	<div>*/}
										{/*		<Typography level="body-xs">{user.name}</Typography>*/}
										{/*		<Typography level="body-xs">{user.role}</Typography>*/}
										{/*	</div>*/}
										{/*</Box>*/}
										<Typography level="body-xs">{user.name}</Typography>
									</td>
									<td>
										<Typography level="body-xs">{user.email}</Typography>
									</td>
									<td>
										<Typography level="body-xs">{user.role}</Typography>
									</td>
									<td>
										{user.role === 'NONE' && <Box sx={{
											display: 'flex',
											gap: 2,
											alignItems: 'center',
											justifyContent: 'end',
											width: '100%'
										}}>
											<IconButton variant="soft" color="primary" size="sm" onClick={() => {
												handleOpen(user)
											}}>
												<EditRoundedIcon/>
											</IconButton>
										</Box>}
									</td>
								</tr>
							))}
							</tbody>
						</Table>
					</Sheet>}

					<Modal open={open} onClose={handleClose} variant="soft">
						<ModalDialog>
							<DialogTitle>Редактирование роли</DialogTitle>
							<form
								onSubmit={handleSubmit(onSubmit)}
							>
								<Stack spacing={2}>
									<Controller
										name={"role"}
										rules={{required: true}}
										fullwidth
										control={control}
										render={({field: {onChange, value}}) => (
											<FormControl fullwidth>
												<FormLabel>Роль</FormLabel>
												<Select
													onChange={(e, newValue) => {
														onChange(newValue);
													}}
													value={value ? value : ""}
												>
													{ROLES.map(role => <Option
														value={role}
														label={role}
														key={role}>{role}</Option>)}
												</Select>
											</FormControl>
										)}
									/>
									<Stack direction="row" spacing={2}>
										<Button type="submit" variant="soft">Сохранить</Button>
										<Button color="danger" variant="soft" onClick={handleClose}>Отмена</Button>
									</Stack>
								</Stack>
							</form>
						</ModalDialog>
					</Modal>
				</Box>

			</Container>
		</Box>
	);
}
