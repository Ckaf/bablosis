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
	Typography, Option, Chip
} from "@mui/joy";
import * as React from 'react';
import {Controller, useForm} from "react-hook-form";
import toast from "react-hot-toast";
import {DELIVERY_STATUSES, ROLES} from "../../config";
import CheckRoundedIcon from '@mui/icons-material/EditRounded';
import PaidRoundedIcon from '@mui/icons-material/EditRounded';
import DeliveryDiningRoundedIcon from '@mui/icons-material/DeliveryDiningRounded';
import HourglassTopRoundedIcon from '@mui/icons-material/HourglassTopRounded';
import CheckCircleOutlineRoundedIcon from '@mui/icons-material/CheckCircleOutlineRounded';
import AutorenewRoundedIcon from '@mui/icons-material/AutorenewRounded';

export const OrdersPage = () => {
	const [orders, setOrders] = useState([]);

	const navigate = useNavigate()
	const {request} = useHttp()
	const {userData} = useAuthContext()

	const fetchOrders = async () => {
		request('/get_my_orders', 'POST').then((data) => {
			setOrders(data.orders.reverse())
		})
	}

	useEffect(() => {
		fetchOrders()
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
					{!orders.length && <Typography>У вас пока нет заказов</Typography>}
					{orders.length > 0 && <Sheet
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
								<th style={{padding: '12px 12px'}}>Баблосы</th>
								<th style={{padding: '12px 12px'}}>Адрес</th>
								<th style={{padding: '12px 12px'}}>Статус</th>
							</tr>
							</thead>
							<tbody>
							{orders.map((order, key) => (
								<tr key={key}>
									<td>
										<Typography level="body-xs">{key + 1}</Typography>
									</td>
									<td>
										<Typography level="body-xs">{order.bablos}</Typography>
									</td>
									<td>
										<Typography level="body-xs">{order.address}</Typography>
									</td>
									<td>
										<Chip
											variant="soft"
											size="sm"
											startDecorator={
												{
													'DONE': <CheckCircleOutlineRoundedIcon/>,
													'ASSEMBLY': <AutorenewRoundedIcon/>,
													'DELIVERY': <DeliveryDiningRoundedIcon/>,
													'NONE': <HourglassTopRoundedIcon/>,
												}[order.status]
											}
											color={
												{
													'NONE': 'primary',
													'DONE': 'neutral',
													'ASSEMBLY': 'warning',
													'DELIVERY': 'success',
												}[order.status]
											}
										>
											{DELIVERY_STATUSES[order.status]}
										</Chip>
										{/*<Typography level="body-xs">{DELIVERY_STATUSES[order.status]}</Typography>*/}
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
