import React, { useEffect, useState } from 'react';
import { useSelector } from 'react-redux';
import styles from './myPage.module.css';

import DonatedShelterListContainer from '../../containers/donatedShelterListContainer';
import DonationList from '../donationList/donationList';
import UserInfo from '../userInfo/userInfo';
import Wallet from '../blockchain/wallet/wallet';
import Nav from '../nav/nav';
import AdoptedAnimalsChart from '../chart/adoptedAnimalsChart';
import ContributionChart from '../chart/contributionChart';


const MyPage = (props) => {
  
  const [userTypeBoolean, setUserTypeBoolean] = useState(true);
  const [isReady, setIsReady] = useState(false);
  const memberInfo = useSelector((state) => state.member.memberInfo);

  useEffect(() => {
    if (memberInfo.data && memberInfo.data.memberRole === 'SHELTER') {
      setUserTypeBoolean(false);
    } else if (memberInfo.data && memberInfo.data.memberRole === 'CONSUMER') {
      setUserTypeBoolean(true);
    } else {
      // props.history.push('/')
    }
    setIsReady(true);
  }, []);

  return ( 
    isReady 
      ? <div className={`${styles.mypage} ${userTypeBoolean ? styles.user : styles.shelter}`}>
          <div className={styles['upper-container']}>
            <Nav role={userTypeBoolean ? "CONSUMER" : "SHELTER"} />
          </div>
          <div className={styles['main-container']}>
            <div className={styles['left-container']}>
              <div className={styles['user-info-box']}>
                <UserInfo userTypeBoolean={userTypeBoolean} />
              </div>
              <div className={styles['wallet-box']}>
                {/* <Wallet userTypeBoolean={userTypeBoolean} /> */}
              </div>
            </div>
            <div className={styles['donation-list-box']}>
              <DonationList userTypeBoolean={userTypeBoolean} />
            </div>
            <div className={styles['etc-boxes']}>
              {
                userTypeBoolean 
                ? <DonatedShelterListContainer /> 
                : <>
                    <ContributionChart />
                    <AdoptedAnimalsChart />
                  </>
              }    
            </div>
          </div>
        </div>
      : <div>loding...</div>
  );
};

export default MyPage;
