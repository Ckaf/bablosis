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
import {DELIVERY_STATUSES, ROLES} from "../../config";
import EditRoundedIcon from '@mui/icons-material/EditRounded';

export const DeliveryPage = () => {
	// const [open, setOpen] = useState(false);
	// const [selectedUserEmail, setSelectedUserEmail] = useState(null);
	const [active, setActive] = useState([]);
	const [free, setFree] = useState([]);

	const navigate = useNavigate()
	const {request} = useHttp()
	const {userData} = useAuthContext()

	const {handleSubmit, control, reset, setValue} = useForm({
		defaultValues: {
			status: 'NONE',
		}
	})

	// const onSubmit = async data => {
	// 	const payload = {email: selectedUserEmail, ...data}
	// 	request(`/set_role?id=${selectedUserEmail}`, 'POST', payload).then(() => {
	// 		toast.success('Роль сохранена')
	// 		handleClose()
	// 	}).catch(() => {})
	// }

	const fetchActive = async () => {
		request('/get_my_orders', 'POST').then((data) => {
			setActive(data.orders.reverse())
		}).catch(() => {
		})
	}

	const fetchFree = async () => {
		request('/get_free_orders', 'POST').then((data) => {
			setFree(data.orders.reverse())
		}).catch(() => {
		})
	}

	const acceptDelivery = (id) => {
		request('/choose_order', 'POST', {orderId: id}).then(() => {
			toast.success('Заказ выбран')
			fetchActive()
			fetchFree()
		}).catch(() => {
		})
	}

	const changeStatusToDelivery = (id) => {
		request('/set_order_status', 'POST', {orderId: id, status: "DELIVERY"}).then(() => {
			toast.success('Статус заказа обновлен')
			fetchActive()
			fetchFree()
		}).catch(() => {
		})
	}

	const changeStatusToDone = (id) => {
		request('/set_order_status', 'POST', {orderId: id, status: "DONE"}).then(() => {
			toast.success('Статус заказа обновлен')
			fetchActive()
			fetchFree()
		}).catch(() => {
		})
	}

	useEffect(() => {
		fetchActive()
		fetchFree()
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
						flexDirection: 'column',
						alignItems: 'start',
						flexWrap: 'wrap',
						justifyContent: 'space-between',
					}}
				>
					<Typography level="h2" component="h1" marginBottom={3}>
						Мои заказы
					</Typography>
					{!active.length && <Typography>У вас нет активных заказов</Typography>}
					{active.length > 0 && <Sheet
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
								<th style={{padding: '12px 12px'}}>id</th>
								<th style={{padding: '12px 12px'}}>Баблосы</th>
								<th style={{padding: '12px 12px'}}>Адрес</th>
								<th style={{padding: '12px 12px'}}></th>
							</tr>
							</thead>
							<tbody>
							{active.map((order, key) => (
								<tr key={order.id}>
									<td>
										<Typography level="body-xs">{order.id}</Typography>
									</td>
									<td>
										<Typography level="body-xs">{order.bablos}</Typography>
									</td>
									<td>
										<Typography level="body-xs">{order.address}</Typography>
									</td>
									<td>
										<Box sx={{
											display: 'flex',
											gap: 2,
											alignItems: 'center',
											justifyContent: 'end',
											width: '100%'
										}}>
											{order.status === 'ASSEMBLY' && <Button variant="soft" color="warning" size="sm" onClick={() => {
												changeStatusToDelivery(order.id)
											}}>
												Собран
											</Button>}
											{order.status === 'DELIVERY' && <Button variant="soft" color="success" size="sm" onClick={() => {
												changeStatusToDone(order.id)
											}}>
												Доставлен
											</Button>}
											{order.status === 'DONE' && <Typography variant="soft" color="neutral" size="sm">
												Завершен
											</Typography>}
										</Box></td>
								</tr>
							))}
							</tbody>
						</Table>
					</Sheet>}
				</Box>

				<Box
					sx={{
						display: 'flex',
						mb: 1,
						gap: 1,
						flexDirection: 'column',
						alignItems: 'start',
						flexWrap: 'wrap',
						justifyContent: 'space-between',
					}}
				>
					<Typography level="h2" component="h1" marginBottom={3}>
						Доступные заказы
					</Typography>
					{!free.length && <Typography>Сейчас нет доступных заказов</Typography>}
					{free.length > 0 && <Sheet
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
								<th style={{padding: '12px 12px'}}>id</th>
								<th style={{padding: '12px 12px'}}>Баблосы</th>
								<th style={{padding: '12px 12px'}}>Адрес</th>
								<th style={{padding: '12px 12px'}}></th>
							</tr>
							</thead>
							<tbody>
							{free.map((order, key) => (
								<tr key={order.id}>
									<td>
										<Typography level="body-xs">{order.id}</Typography>
									</td>
									<td>
										<Typography level="body-xs">{order.bablos}</Typography>
									</td>
									<td>
										<Typography level="body-xs">{order.address}</Typography>
									</td>
									<td>
										<Box sx={{
											display: 'flex',
											gap: 2,
											alignItems: 'center',
											justifyContent: 'end',
											width: '100%'
										}}>
											<Button variant="soft" color="primary" size="sm" onClick={() => {
												acceptDelivery(order.id)
											}}>
												Выбрать
											</Button>
										</Box>
									</td>
								</tr>
							))}
							</tbody>
						</Table>
					</Sheet>}


				</Box>

			</Container>
		</Box>
	);
}
