import { PetDetailType, PetEditType, PetListType, PetProfileImage } from '../interface/pet';
import { image, security } from './instance';

// 반려동물 전체 조회
export const getPetList = async () => {
	const response = await security.get<PetListType[]>('pets', {
		'headers': {
			'Authorization': 'Bearer ' + localStorage.getItem('access_token'),
		}
	});
	return response.data;
}

// 특정 보호소 동물 리스트 조회
export const getShelterPetList = async (id: number) => {
	const response = await security.get<PetListType[]>(`shelters/${id}/pets`, {
		'headers': {
			'Authorization': 'Bearer ' + localStorage.getItem('access_token'),
		}
	});
	return response.data;
}

// 반려동물 등록
export const registerPet = async ( petInfo: PetEditType ) => {
	const response = await security.post<undefined>('pets', {
		'headers': {
			'Authorization': 'Bearer ' + localStorage.getItem('access_token'),
		},
		petInfo,
	});
	return response.data;
}

// 반려동물 정보 조회
export const getPet = async ( id: number ) => {
	const response = await security.get<PetDetailType>(`pets/${id}`, {
		  'headers': {
		    'Authorization': 'Bearer ' + localStorage.getItem('access_token'),
		  }
	});
	return response.data;
}

// 반려동물 정보 수정
export const modifyPet = async ( petInfo: PetEditType ) => {
	const response = await security.put<PetDetailType>(`pets/${petInfo.id}`, {
		'headers': {
			'Authorization': 'Bearer ' + localStorage.getItem('access_token'),
		},
		petInfo,
	});
	return response.data;
}

// 반려동물 삭제
export const deletePet = async ( id: number ) => {
	const response = await security.delete<undefined>(`pets/${id}`, {
		'headers': {
			'Authorization': 'Bearer ' + localStorage.getItem('access_token'),
		}
	});
	return response.data;
}

// 반려동물 이미지 삽입
export const setProfileImage = async ( profileImage: PetProfileImage) => {
	const response = await image.post<undefined>('pets/${petId}/image', profileImage, {
		'headers': {
			'Authorization': 'Bearer ' + localStorage.getItem('access_token'),
		}
	});
	return response.data;
}