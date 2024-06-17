import {
	Button,
	DialogTitle,
	FormControl,
	FormLabel, Input,
	Modal,
	ModalDialog,
	Option,
	Select,
	Stack, Textarea,
	Typography
} from "@mui/joy";
import {Controller, useForm} from "react-hook-form";
import {ROLES} from "../config";
import * as React from "react";
import {useHttp} from "../hooks/http.hook";
import {useAuthContext} from "../context/AuthContext";
import toast from "react-hot-toast";
import {useNavigate} from "react-router-dom";
import {useEffect, useState} from "react";

export const AddPostModal = ({isOpen, setIsOpen}) => {
	const [channels, setChannels] = useState([]);
	const {request} = useHttp()
	const {userData} = useAuthContext()
	const navigate = useNavigate()

	const {handleSubmit, control, reset, setValue} = useForm({
		defaultValues: {
			channelId: '',
			date: '',
			msg: '',
		}
	})

	const onSubmit = async data => {
		request(`/post`, 'POST', data).then(() => {
			toast.success('Пост создан')
			handleClose()
		}).catch(() => {})
	}

	const fetchChannels = () => {
		request(`/get_my_channels`, 'POST').then((resp) => {
			if (resp && resp.channels[0].name !== 'null') {
				setChannels(resp.channels)
				setValue('channelId', resp.channels[0].name)
			} else setChannels([])
		}).catch(() => {})
	}

	const handleClose = () => {
		reset()
		setIsOpen(false)
	}

	useEffect(() => {
		fetchChannels()
	}, [isOpen]);


	return (
		<Modal open={isOpen} onClose={handleClose} variant="soft">
			<ModalDialog>
				<DialogTitle>Создать задание на публикацию</DialogTitle>
				<form
					onSubmit={handleSubmit(onSubmit)}
				>
					<Stack spacing={2}>
						<Controller
							name={"channelId"}
							rules={{required: true}}
							fullwidth
							control={control}
							render={({field: {onChange, value}}) => (
								<FormControl fullwidth>
									<FormLabel>Канал</FormLabel>
									<Select
										onChange={(e, newValue) => {
											onChange(newValue);
										}}
										value={value ? value : ""}
									>
										{channels.map(channel => <Option
											value={channel.name}
											label={channel.name}
											key={channel.name}>{channel.name}</Option>)}
									</Select>
								</FormControl>
							)}
						/>
						<Controller
							name={"date"}
							required
							fullwidth
							control={control}
							render={({field: {onChange, value}}) => (
								<FormControl>
									<FormLabel>Дата и время</FormLabel>
									<Input type="datetime-local" required value={value} onChange={onChange}/>
								</FormControl>
							)}
						/>
						<Controller
							name={"msg"}
							required
							fullwidth
							control={control}
							render={({field: {onChange, value}}) => (
								<FormControl>
									<FormLabel>Дискурс</FormLabel>
									<Textarea minRows={3} autoFocus required value={value} onChange={onChange}/>
								</FormControl>
							)}
						/>
						<Stack direction="row" spacing={2}>
							<Button type="submit" variant="soft">Опубликовать</Button>
							<Button color="danger" variant="soft" onClick={handleClose}>Отмена</Button>
						</Stack>
					</Stack>
				</form>
			</ModalDialog>
		</Modal>
	)
}
