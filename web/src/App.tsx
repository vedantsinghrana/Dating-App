import { Navigate, Route, Routes } from 'react-router-dom';
import { ThemeProvider } from './theme/ThemeProvider';
import { AppLayout } from './routes/AppLayout';
import { RequireAuth } from './routes/RequireAuth';
import { LoginPage } from './features/auth/LoginPage';
import { RegisterPage } from './features/auth/RegisterPage';
import { DiscoverPage } from './features/discovery/DiscoverPage';
import { ProfilePage } from './features/profile/ProfilePage';
import { MatchesPage } from './features/matches/MatchesPage';
import { ChatIndexPage } from './features/chat/ChatIndexPage';
import { ChatPage } from './features/chat/ChatPage';

function App() {
  return (
    <ThemeProvider>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />

        <Route
          element={
            <RequireAuth>
              <AppLayout />
            </RequireAuth>
          }
        >
          <Route path="/discover" element={<DiscoverPage />} />
          <Route path="/matches" element={<MatchesPage />} />
          <Route path="/chat" element={<ChatIndexPage />} />
          <Route path="/chat/:matchId" element={<ChatPage />} />
          <Route path="/profile" element={<ProfilePage />} />
        </Route>

        <Route path="/" element={<Navigate to="/discover" replace />} />
        <Route path="*" element={<Navigate to="/discover" replace />} />
      </Routes>
    </ThemeProvider>
  );
}

export default App;
