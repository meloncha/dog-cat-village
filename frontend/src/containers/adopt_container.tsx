import React, { useState } from "react";
import AdoptDetail from "../components/adopt/adoptDetail/adoptDetail";
import AdoptList from "../components/adopt/adoptList/adoptList";
import { Search } from "../components/common/common";
import Animal from "../components/user/main/animal/animal";
import { AdoptDetailType, AdoptListType } from "../interface/adopt";

const AdoptContainer = () => {
  const adoptList: AdoptListType[] = [
    {
      id: 1,
      petName: "petname",
      name: "username",
      createdDate: "2021.03.02",
      acceptStatus: "ACCEPTED",
    },
    {
      id: 2,
      petName: "petname",
      name: "username",
      createdDate: "2021.03.02",
      acceptStatus: "PENDING",
    },
    {
      id: 3,
      petName: "petname",
      name: "username",
      createdDate: "2021.03.02",
      acceptStatus: "REFUSED",
    },
    {
      id: 4,
      petName: "petname",
      name: "username",
      createdDate: "2021.03.02",
      acceptStatus: "ACCEPTED",
    },
  ];

  const [searchInput, setSearchInput] = useState({
    adopt: "",
    type: "member",
    input: "",
  });
  const [resultAdoptList, setResultAdoptList] = useState<AdoptListType[]>(
    adoptList
  );
  const [selectedAdopt, setSelectedAdopt] = useState<AdoptDetailType | null>(
    null
  );

  const onChange = (
    e:
      | React.ChangeEvent<HTMLSelectElement>
      | React.ChangeEvent<HTMLInputElement>
  ) => {
    const { name, value } = e.target;

    setSearchInput({
      ...searchInput,
      [name]: value,
    });
  };

  const onSearch = () => {
    if (searchInput.type === "member") {
      setResultAdoptList(
        adoptList.filter(
          (adopt) =>
            adopt.acceptStatus.includes(searchInput.adopt) &&
            adopt.name &&
            adopt.name.includes(searchInput.input)
        )
      );
    } else if (searchInput.type === "pet") {
      setResultAdoptList(
        adoptList.filter(
          (adopt) =>
            adopt.acceptStatus.includes(searchInput.adopt) &&
            adopt.petName.includes(searchInput.input)
        )
      );
    }
  };

  const goToBack = () => {
    setSelectedAdopt(null);
  };

  const onClick = (adoptId: number) => {
    setSelectedAdopt({
      id: 1,
      petId: 1,
      petName: "동물 이름",
      consumer: {
        id: 1,
        profileImage:
          "http://ojsfile.ohmynews.com/STD_IMG_FILE/2007/1128/IE000838568_STD.jpg",
        name: "멤버 닉네임",
        email: "ssafy@ssafy.com",
        phoneNumber: "01020457251",
      },
      name: "멤버실제이름",
      sex: "FEMALE",
      age: "25",
      address: "서울",
      description: "입양 사유",
      day: "주말 선호",
      time: "오후 시간 선호",
      acceptStatus: "PENDING",
      createdDate: "20210321",
    });
  };

  const onSubmit = (e: React.MouseEvent<HTMLButtonElement>) => {
    const { value } = e.currentTarget;
    console.log(value);
  };

  const selectList = [
    {
      name: "adopt",
      options: [
        { value: "", option: "모두" },
        { value: "ACCEPTED", option: "입양 완료" },
        { value: "PENDING", option: "진행 중" },
        { value: "REFUSED", option: "입양 거절" },
      ],
    },
    {
      name: "type",
      options: [
        { value: "member", option: "유저" },
        { value: "pet", option: "동물" },
      ],
    },
  ];

  return (
    <>
      {selectedAdopt ? (
        <AdoptDetail
          selectedAdopt={selectedAdopt}
          type="shelter"
          goToBack={goToBack}
          onSubmit={onSubmit}
        />
      ) : (
        <>
          <Search
            selectList={selectList}
            selectValue={[searchInput.adopt, searchInput.type]}
            inputName="input"
            inputValue={searchInput.input}
            onSearch={onSearch}
            onChange={onChange}
            placeholder="검색어"
            inputSize="input-medium"
          />
          <AdoptList
            adoptList={resultAdoptList}
            type="shelter"
            onClick={onClick}
          />
        </>
      )}
    </>
  );
};

export default AdoptContainer;
