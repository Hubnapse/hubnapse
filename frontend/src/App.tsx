import "./App.css";
import { Link, Route, Routes } from "react-router-dom";
import FeedPage from "./pages/FeedPage";
import ProfilePage from "./pages/ProfilePage";
import AuthPanel from "./components/auth/AuthPanel";
import CurrentUserBar from "./components/auth/CurrentUserBar";
import { useAuth } from "./hooks/useAuth";

function App() {
  const auth = useAuth();

  return (
    <main className="post-list-page">
      <h1>
        <Link to="/" className="app-title-link">
          Hubnapse
        </Link>
      </h1>

      {!auth.initializing &&
        (auth.user ? (
          <CurrentUserBar
            user={auth.user}
            submitting={auth.submitting}
            error={auth.authError}
            onLogout={auth.logout}
          />
        ) : (
          <AuthPanel
            submitting={auth.submitting}
            error={auth.authError}
            onLogin={auth.login}
            onRegister={auth.register}
          />
        ))}

      <Routes>
        <Route path="/" element={<FeedPage auth={auth} />} />
        <Route path="/users/:username" element={<ProfilePage auth={auth} />} />
      </Routes>
    </main>
  );
}

export default App;
