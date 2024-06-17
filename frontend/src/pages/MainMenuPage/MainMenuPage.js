import {useEffect, useState} from "react";
import {useNavigate} from "react-router-dom";
import {useHttp} from "../../hooks/http.hook";
import {useAuthContext} from "../../context/AuthContext";
import {Box, Button, Container, CssBaseline, Typography} from "@mui/joy";
import * as React from 'react';
import {AddPostModal, AddTelegramModal, AddOrderModal, AddBablosModal} from "../../components";

export const MainMenuPage = () => {
	const [isTgAdded, setIsTgAdded] = useState(false);
	const [isTgModalOpen, setIsTgModalOpen] = useState(false);
	const [isOrderModalOpen, setIsOrderModalOpen] = useState(false);
	const [isPostModalOpen, setIsPostModalOpen] = useState(false);
	const [isBablosModalOpen, setIsBablosModalOpen] = useState(false);

	const navigate = useNavigate()
	const {request} = useHttp()
	const {userData} = useAuthContext()

	const checkTg = async () => {
		if (userData.user.role === 'USER') {
			request('/get_my_channels', 'POST',).then((data) => {
				if (data && data.channels[0].name !== null) setIsTgAdded(true)
			})
		}
	}

	useEffect(() => {
		checkTg()
	}, [userData.user.role]);


	return (
		<Box component="main"
		     sx={{paddingTop: 5, height: '100vh', width: '100%'}}>
			<CssBaseline/>
			<Container>
				{userData.user.role === 'ADMIN' &&
					<Box
						mb={5}>
						<Typography level="h3" color="primary" varinat="plain" marginBottom={3}>Действия</Typography>
						<Box sx={{display: 'flex', flexDirection: 'row', gap: 3}}>
							<Button
								color='primary'
								variant="soft"
								size="lg"
								onClick={() => {
									navigate('/users')
								}}
							>
								Управление пользователями
							</Button>
						</Box>
					</Box>
				}

				{userData.user.role === 'COURIER' &&
					<Box
						mb={5}>
						<Typography level="h3" color="primary" varinat="plain" marginBottom={3}>Действия</Typography>
						<Box sx={{display: 'flex', flexDirection: 'row', gap: 3}}>
							<Button
								color='primary'
								variant="soft"
								size="lg"
								onClick={() => {
									navigate('/delivery')
								}}
							>
								Выбрать доставку
							</Button>
						</Box>
					</Box>
				}

				{userData.user.role === 'ISHTAR' &&
					<Box
						mb={5}>
						<Typography level="h3" color="primary" varinat="plain" marginBottom={3}>Действия</Typography>
						<Box sx={{display: 'flex', flexDirection: 'row', gap: 3}}>
							<Button
								color='primary'
								variant="soft"
								size="lg"
								onClick={() => {
									setIsBablosModalOpen(true)
								}}
							>
								Ввести поставку баблоса
							</Button>
						</Box>
					</Box>
				}

				{(userData.user.role === 'USER' && !isTgAdded) &&
					<Box
						mb={5}>
						<Typography level="h3" color="primary" varinat="plain" marginBottom={3}>Гламур и дискурс</Typography>
						<Box sx={{display: 'flex', flexDirection: 'row', gap: 3}}>
							<Button
								color='primary'
								variant="soft"
								size="lg"
								onClick={() => {
									setIsTgModalOpen(true)
								}}
							>
								Привязать телеграм
							</Button>
						</Box>
					</Box>
				}

				{(userData.user.role === 'USER' && isTgAdded) &&
					<>
						<Box
							mb={5}>
							<Typography level="h3" color="primary" varinat="plain" marginBottom={3}>Доставки</Typography>
							<Box sx={{display: 'flex', flexDirection: 'row', gap: 3}}>
								<Button
									color='primary'
									variant="soft"
									size="lg"
									onClick={() => {
										setIsOrderModalOpen(true)
									}}
								>
									Заказать доставку баблоса
								</Button>
								<Button
									color='primary'
									variant="soft"
									size="lg"
									onClick={() => {
										navigate('/my-orders')
									}}
								>
									Мои заказы
								</Button>
							</Box>
						</Box>
						<Box
							mb={5}>
							<Typography level="h3" color="primary" varinat="plain" marginBottom={3}>Гламур и дискурс</Typography>
							<Box sx={{display: 'flex', flexDirection: 'row', gap: 3}}>
								<Button
									color='primary'
									variant="soft"
									size="lg"
									onClick={() => {
										setIsPostModalOpen(true)
									}}
								>
									Создать задание на публикацию
								</Button>
								{/*<Button*/}
								{/*	color='primary'*/}
								{/*	variant="soft"*/}
								{/*	size="lg"*/}
								{/*	onClick={() => {*/}
								{/*		navigate('/')*/}
								{/*	}}*/}
								{/*>*/}
								{/*	Мои публикации*/}
								{/*</Button>*/}
							</Box>
						</Box>
					</>
				}


				{userData.user.role === 'USER' && <AddOrderModal isOpen={isOrderModalOpen} setIsOpen={setIsOrderModalOpen}/>}
				{userData.user.role === 'USER' && <AddTelegramModal isOpen={isTgModalOpen} setIsOpen={setIsTgModalOpen} refetch={checkTg} />}
				{userData.user.role === 'USER' && <AddPostModal isOpen={isPostModalOpen} setIsOpen={setIsPostModalOpen} />}
				{userData.user.role === 'ISHTAR' && <AddBablosModal isOpen={isBablosModalOpen} setIsOpen={setIsBablosModalOpen} />}
			</Container>
		</Box>
	);
}
