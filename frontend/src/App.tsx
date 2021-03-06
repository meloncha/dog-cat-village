import React from 'react';
import { Route, Switch } from 'react-router-dom';
import styles from './App.module.css';
import MemberContainer from './containers/memberContainer';
import ProfileContainer from './containers/profileContainer';
import UserMainPage from './components/userMainPage/userMainPage';
import UserStreamingPage from './components/userStreamingPage/userStreamingPage';
import ErrorAlert, { ProtectedRouteConsumer } from './components/error/errorAlert';
import PasswordContainer from './containers/passwordContainer';
import ConfirmSignUp from './components/submain/confirmSignUp/confirmSignUp';
import Main from '../src/components/shelter/main/main';
import PetListContainer from './containers/petListContainer';
import ShelterListContainer from './containers/shelterListContainer';


function App() {
  return (
    <div className={styles.app}>
      <Switch>
        <Route path="/streaming/:shelterId/:memberId" component={UserStreamingPage} exact/>
        <ProtectedRouteConsumer path="/shelter" Component={ShelterListContainer} exact/>
        
        <ProtectedRouteConsumer path="/user" Component={UserMainPage} exact/>
        {/* <ProtectedRouteShelter path="/main" Component={Main} exact/> */}

        <Route path="/profile" component={ProfileContainer} exact/>


        {/* <ProtectedRouteConsumer path="/streaming" Component={StreamingListPage} exact/> */}
        {/* <Route path="/streaming" component={StreamingListPage} exact/> */}

        <ProtectedRouteConsumer path="/pet" Component={PetListContainer} exact/>

        <Route path="/signup/:result/:id" component={ConfirmSignUp} exact/>
        <Route path="/signup/:result" component={ConfirmSignUp} exact/>
        <Route path="/password/:auth" component={PasswordContainer} exact/>

        {/* <Route path="/blockchain:token" component={ChargeFinish}/>/ */}
        { /* https://j4b106.p.ssafy.io/blockchain?pg_token=234ad479fb1863f54c00 */}
        
        <Route path="/:id" component={Main} exact/>
        <Route path="/" component={MemberContainer} exact/>
        <Route>
          <ErrorAlert message="????????? ?????? ?????????."/>
        </Route>
      </Switch>
    </div>
  );
}

export default App;