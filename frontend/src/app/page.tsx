'use client';
import { useState, useEffect, useRef } from 'react';

export default function Home() {
  const [city, setCity] = useState('');
  const [weather, setWeather] = useState<any>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [broadcasts, setBroadcasts] = useState<any[]>([]);
  const [activityLog, setActivityLog] = useState<any[]>([]);
  const [wsStatus, setWsStatus] = useState<'disconnected' | 'connecting' | 'connected'>('disconnected');
  const [showActivityLog, setShowActivityLog] = useState(false);
  const [showCityManager, setShowCityManager] = useState(false);
  const [broadcastCities, setBroadcastCities] = useState<string[]>([]);
  const [defaultCities, setDefaultCities] = useState<string[]>([]);
  const [loadingCities, setLoadingCities] = useState(false);
  const wsRef = useRef<WebSocket | null>(null);
  const activityLogRef = useRef<HTMLDivElement>(null);

  // Auto-scroll activity log to bottom
  useEffect(() => {
    if (activityLogRef.current && showActivityLog) {
      activityLogRef.current.scrollTop = activityLogRef.current.scrollHeight;
    }
  }, [activityLog, showActivityLog]);

  const addActivity = (type: 'search' | 'broadcast' | 'connection' | 'error' | 'delete' | 'city_update', message: string, city?: string) => {
    setActivityLog(prev => [
      ...prev,
      {
        type,
        message,
        city,
        timestamp: new Date()
      }
    ].slice(-50)); // Keep last 50 activities
  };

  // Fetch broadcast cities list
  const fetchBroadcastCities = async () => {
    setLoadingCities(true);
    try {
      const res = await fetch('http://localhost:8080/api/weather/broadcast-cities');
      const data = await res.json();
      setBroadcastCities(data.cities || []);
      setDefaultCities(data.defaultCities || []);
    } catch (e) {
      console.error('Failed to fetch broadcast cities:', e);
      addActivity('error', 'Failed to fetch broadcast cities list');
    } finally {
      setLoadingCities(false);
    }
  };

  // Load broadcast cities when city manager is opened
  useEffect(() => {
    if (showCityManager) {
      fetchBroadcastCities();
    }
  }, [showCityManager]);

  // Delete a city from broadcast list
  const handleDeleteCity = async (cityName: string) => {
    try {
      const res = await fetch(`http://localhost:8080/api/weather/broadcast-cities?city=${encodeURIComponent(cityName)}`, {
        method: 'DELETE'
      });
      const data = await res.json();
      
      if (data.success) {
        addActivity('delete', `Removed from broadcast list`, cityName);
        // Don't fetch - wait for WebSocket update
        
        // Remove from broadcasts display
        setBroadcasts(prev => prev.filter(b => b.city !== cityName));
      } else {
        addActivity('error', data.message || 'Failed to remove city', cityName);
        alert(data.message || 'Failed to remove city');
      }
    } catch (e) {
      console.error('Failed to delete city:', e);
      addActivity('error', 'Failed to remove city from broadcast list', cityName);
      alert('Failed to remove city. Please try again.');
    }
  };

  // Clear all non-default cities
  const handleClearAllCities = async () => {
    if (!confirm('Are you sure you want to remove all non-default cities from the broadcast list?')) {
      return;
    }

    try {
      const res = await fetch('http://localhost:8080/api/weather/broadcast-cities/clear', {
        method: 'DELETE'
      });
      const data = await res.json();
      
      if (data.success) {
        addActivity('delete', `Cleared ${data.removedCount} non-default cities`);
        // Don't fetch - wait for WebSocket update
        
        // Keep only default cities in broadcasts
        setBroadcasts(prev => prev.filter(b => defaultCities.includes(b.city)));
      } else {
        addActivity('error', 'Failed to clear cities');
        alert('Failed to clear cities');
      }
    } catch (e) {
      console.error('Failed to clear cities:', e);
      addActivity('error', 'Failed to clear broadcast cities');
      alert('Failed to clear cities. Please try again.');
    }
  };

  // WebSocket connection
  useEffect(() => {
    const connectWebSocket = () => {
      setWsStatus('connecting');
      addActivity('connection', 'Connecting to WebSocket server...');
      const ws = new WebSocket('ws://localhost:5001');
      
      ws.onopen = () => {
        console.log('WebSocket connected');
        setWsStatus('connected');
        addActivity('connection', 'WebSocket connected successfully');
        // Fetch initial city list on connection
        fetchBroadcastCities();
      };

      ws.onmessage = (event) => {
        console.log('Received broadcast:', event.data);
        try {
          const message = event.data;
          
          // Handle city list updates
          if (message.startsWith('[CITY_LIST_UPDATE]')) {
            const cityListStr = message.substring('[CITY_LIST_UPDATE]'.length).trim();
            const cities = cityListStr ? cityListStr.split(',').map((c: string) => c.trim()) : [];
            
            console.log('City list updated:', cities);
            setBroadcastCities(cities);
            addActivity('city_update', `Broadcast list updated: ${cities.length} cities`);
            
            return;
          }
          
          // Handle weather broadcasts
          if (message.startsWith('[BROADCAST]')) {
            const jsonStart = message.indexOf('{');
            if (jsonStart !== -1) {
              const jsonStr = message.substring(jsonStart);
              const weatherData = JSON.parse(jsonStr);
              
              // Add to broadcasts list - keep only latest for each city
              setBroadcasts(prev => {
                const filtered = prev.filter(b => b.city !== weatherData.city);
                return [
                  { ...weatherData, timestamp: new Date() },
                  ...filtered
                ].slice(0, 10);
              });

              addActivity('broadcast', `Received weather update: ${weatherData.temperature}°C`, weatherData.city);

              // If this is the currently searched city, update main display
              if (weather && weatherData.city === weather.city) {
                setWeather(weatherData);
              }
            }
          }
        } catch (e) {
          console.error('Failed to parse broadcast:', e);
          addActivity('error', 'Failed to parse broadcast message');
        }
      };

      ws.onerror = (error) => {
        console.warn('WebSocket connection failed');
        setWsStatus('disconnected');
        addActivity('error', 'WebSocket connection failed - server may not be running');
      };

      ws.onclose = () => {
        console.log('WebSocket disconnected');
        setWsStatus('disconnected');
        addActivity('connection', 'WebSocket disconnected');
        if (wsStatus === 'connected') {
          console.log('Reconnecting in 5s...');
          setTimeout(connectWebSocket, 5000);
        }
      };

      wsRef.current = ws;
    };

    connectWebSocket();

    return () => {
      if (wsRef.current) {
        wsRef.current.close();
      }
    };
  }, [weather]);

  const handleSearch = async () => {
    if (!city) return;
    setLoading(true);
    setWeather(null);
    setError(null);
    
    addActivity('search', `Searching weather for "${city}"...`, city);

    try {
      const res = await fetch(`http://localhost:8080/api/weather/search?city=${encodeURIComponent(city)}`);
      const data = await res.json();
      if (data.error) {
        setError(data.error);
        addActivity('error', `Search failed: ${data.error}`, city);
      } else {
        setWeather(data);
        addActivity('search', `Weather data received: ${data.temperature}°C (added to broadcast list)`, city);
        // Don't manually fetch cities - WebSocket will update
      }
    } catch (e: any) {
      setError("Failed to fetch from backend.");
      addActivity('error', 'Failed to connect to backend server', city);
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  const getWeatherIcon = (code: number) => {
    if (code === 0) return '☀️';
    if (code <= 3) return '🌤️';
    if (code <= 48) return '☁️';
    if (code <= 67) return '🌧️';
    if (code <= 77) return '🌨️';
    if (code <= 82) return '⛈️';
    return '🌩️';
  };

  const getActivityIcon = (type: string) => {
    switch (type) {
      case 'search': return '🔍';
      case 'broadcast': return '📡';
      case 'connection': return '🔌';
      case 'error': return '❌';
      case 'delete': return '🗑️';
      case 'city_update': return '🏙️';
      default: return '📝';
    }
  };

  const getActivityColor = (type: string) => {
    switch (type) {
      case 'search': return 'text-blue-600';
      case 'broadcast': return 'text-green-600';
      case 'connection': return 'text-purple-600';
      case 'error': return 'text-red-600';
      case 'delete': return 'text-orange-600';
      case 'city_update': return 'text-cyan-600';
      default: return 'text-gray-600';
    }
  };

  return (
    <main className="flex flex-col items-center min-h-screen bg-gradient-to-br from-sky-100 to-blue-200 p-6">
      <div className="w-full max-w-6xl">
        {/* Header */}
        <div className="text-center mb-8">
          <h1 className="text-4xl font-bold text-sky-800 mb-2">
            🌦️ Live Weather System
          </h1>
          <div className="flex items-center justify-center gap-4 text-sm flex-wrap">
            <div className="flex items-center gap-2">
              <span className={`inline-block w-2 h-2 rounded-full ${
                wsStatus === 'connected' ? 'bg-green-500' : 
                wsStatus === 'connecting' ? 'bg-yellow-500' : 'bg-red-500'
              }`}></span>
              <span className="text-gray-600">
                {wsStatus === 'connected' ? 'Live updates enabled' : 
                 wsStatus === 'connecting' ? 'Connecting...' : 'Reconnecting...'}
              </span>
            </div>
            <button
              onClick={() => setShowCityManager(!showCityManager)}
              className="bg-white px-3 py-1 rounded-lg text-gray-700 hover:bg-gray-50 shadow-sm transition"
            >
              {showCityManager ? '🏙️ Hide Cities' : '🏙️ Manage Cities'} ({broadcastCities.length})
            </button>
            <button
              onClick={() => setShowActivityLog(!showActivityLog)}
              className="bg-white px-3 py-1 rounded-lg text-gray-700 hover:bg-gray-50 shadow-sm transition"
            >
              {showActivityLog ? '📋 Hide Log' : '📋 Activity Log'} ({activityLog.length})
            </button>
          </div>
        </div>

        {/* Search Bar */}
        <div className="flex gap-2 mb-8 justify-center">
          <input
            type="text"
            value={city}
            onChange={(e) => setCity(e.target.value)}
            onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
            placeholder="Enter city name"
            className="border border-gray-300 rounded-lg px-4 py-3 w-80 focus:outline-none focus:ring-2 focus:ring-sky-500 shadow-sm"
          />
          <button 
            onClick={handleSearch} 
            disabled={loading}
            className="bg-sky-600 text-white px-6 py-3 rounded-lg hover:bg-sky-700 disabled:opacity-50 shadow-md transition"
          >
            {loading ? 'Searching...' : 'Search'}
          </button>
        </div>

        {error && (
          <div className="text-center mb-6">
            <p className="text-red-600 bg-red-50 px-4 py-2 rounded-lg inline-block">
              {error}
            </p>
          </div>
        )}

        {/* Main Weather Display */}
        {weather && !error && (
          <div className="bg-white rounded-2xl shadow-xl p-8 mb-8 max-w-2xl mx-auto">
            <div className="text-center">
              <div className="text-6xl mb-4">
                {getWeatherIcon(weather.weathercode)}
              </div>
              <h2 className="text-3xl font-bold text-gray-800 mb-2">
                {weather.city}, {weather.country}
              </h2>
              <p className="text-5xl font-bold text-sky-600 mb-6">
                {weather.temperature}°C
              </p>
              
              <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-left">
                <div className="bg-sky-50 p-3 rounded-lg">
                  <p className="text-xs text-gray-500 mb-1">High</p>
                  <p className="text-xl font-semibold text-gray-800">
                    ⬆️ {weather.temperature_max}°C
                  </p>
                </div>
                <div className="bg-sky-50 p-3 rounded-lg">
                  <p className="text-xs text-gray-500 mb-1">Low</p>
                  <p className="text-xl font-semibold text-gray-800">
                    ⬇️ {weather.temperature_min}°C
                  </p>
                </div>
                <div className="bg-sky-50 p-3 rounded-lg">
                  <p className="text-xs text-gray-500 mb-1">Wind</p>
                  <p className="text-xl font-semibold text-gray-800">
                    💨 {weather.windspeed} m/s
                  </p>
                </div>
                <div className="bg-sky-50 p-3 rounded-lg">
                  <p className="text-xs text-gray-500 mb-1">Rain</p>
                  <p className="text-xl font-semibold text-gray-800">
                    🌧️ {weather.precipitation_sum} mm
                  </p>
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4 mt-4">
                <div className="bg-orange-50 p-3 rounded-lg">
                  <p className="text-xs text-gray-500 mb-1">Sunrise</p>
                  <p className="text-lg font-semibold text-gray-800">
                    🌅 {new Date(weather.sunrise).toLocaleTimeString()}
                  </p>
                </div>
                <div className="bg-purple-50 p-3 rounded-lg">
                  <p className="text-xs text-gray-500 mb-1">Sunset</p>
                  <p className="text-lg font-semibold text-gray-800">
                    🌇 {new Date(weather.sunset).toLocaleTimeString()}
                  </p>
                </div>
              </div>

              <p className="text-xs text-gray-400 mt-4">
                Last updated: {new Date(weather.time).toLocaleString()}
              </p>
            </div>
          </div>
        )}

        {/* Live Broadcasts */}
        {broadcasts.length > 0 && (
          <div className="max-w-6xl mx-auto mb-8">
            <h3 className="text-2xl font-bold text-gray-800 mb-4 text-center">
              📡 Live Broadcasts
            </h3>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              {broadcasts.map((broadcast, index) => (
                <div 
                  key={index}
                  className="bg-white rounded-lg shadow-md p-4 hover:shadow-lg transition cursor-pointer"
                  onClick={() => {
                    setWeather(broadcast);
                    addActivity('search', `Viewing broadcast data`, broadcast.city);
                  }}
                >
                  <div className="flex justify-between items-start mb-2">
                    <h4 className="text-lg font-semibold text-gray-800">
                      {broadcast.city}
                    </h4>
                    <span className="text-2xl">
                      {getWeatherIcon(broadcast.weathercode)}
                    </span>
                  </div>
                  <p className="text-3xl font-bold text-sky-600 mb-2">
                    {broadcast.temperature}°C
                  </p>
                  <div className="text-sm text-gray-600 space-y-1">
                    <p>💨 {broadcast.windspeed} m/s</p>
                    <p>🌧️ {broadcast.precipitation_sum} mm</p>
                  </div>
                  <p className="text-xs text-gray-400 mt-2">
                    {broadcast.timestamp.toLocaleTimeString()}
                  </p>
                </div>
              ))}
            </div>
          </div>
        )}

        {broadcasts.length === 0 && wsStatus === 'connected' && (
          <div className="text-center text-gray-500 mt-8 mb-8">
            <p>Waiting for weather broadcasts...</p>
            <p className="text-sm">Updates arrive every 60 seconds</p>
          </div>
        )}

         {/* City Manager Panel */}
        {showCityManager && (
          <div className="bg-white rounded-xl shadow-lg p-6 mb-8 max-w-6xl mx-auto">
            <div className="flex justify-between items-center mb-4">
              <h3 className="text-xl font-bold text-gray-800">🏙️ Broadcast Cities</h3>
              <button
                onClick={handleClearAllCities}
                className="text-sm text-red-600 hover:text-red-700 px-3 py-1 rounded hover:bg-red-50 transition"
              >
                Clear All 
              </button>
            </div>

            {loadingCities ? (
              <div className="text-center py-8 text-gray-500">
                <div className="animate-spin inline-block w-8 h-8 border-4 border-sky-500 border-t-transparent rounded-full mb-2"></div>
                <p>Loading cities...</p>
              </div>
            ) : (
              <div className="space-y-2 max-h-96 overflow-y-auto">
                {broadcastCities.length === 0 ? (
                  <p className="text-gray-400 text-center py-8">No cities in broadcast list</p>
                ) : (
                  broadcastCities.map((cityName) => {
                    const isDefault = defaultCities.includes(cityName);
                    return (
                      <div 
                        key={cityName}
                        className="flex items-center justify-between bg-gray-50 rounded-lg p-4 hover:bg-gray-100 transition"
                      >
                        <div className="flex items-center gap-3">
                          <span className="text-2xl">🏙️</span>
                          <div>
                            <p className="font-semibold text-gray-800">{cityName}</p>
                            {isDefault && (
                              <span className="text-xs text-blue-600 bg-blue-50 px-2 py-0.5 rounded">
                                Default City
                              </span>
                            )}
                          </div>
                        </div>
                        <button
                          onClick={() => handleDeleteCity(cityName)}
                          disabled={isDefault}
                          className={`px-4 py-2 rounded-lg transition ${
                            isDefault 
                              ? 'bg-gray-200 text-gray-400 cursor-not-allowed' 
                              : 'bg-red-500 text-white hover:bg-red-600'
                          }`}
                          title={isDefault ? 'Cannot remove default cities' : 'Remove from broadcast list'}
                        >
                          {isDefault ? 'Protected' : 'Remove'}
                        </button>
                      </div>
                    );
                  })
                )}
              </div>
            )}

            <div className="mt-4 p-4 bg-blue-50 rounded-lg">
              <p className="text-sm text-blue-800">
                <strong>Real-time sync:</strong> When you search for a city, it's automatically added to the broadcast list. 
                All changes sync instantly across all connected clients via WebSocket.
              </p>
            </div>
          </div>
        )}

        {/* Activity Log Panel */}
        {showActivityLog && (
          <div className="bg-white rounded-xl shadow-lg p-4 mb-8 max-w-6xl mx-auto">
            <div className="flex justify-between items-center mb-3">
              <h3 className="text-lg font-bold text-gray-800">📋 Activity Log</h3>
              <button
                onClick={() => setActivityLog([])}
                className="text-sm text-red-600 hover:text-red-700 px-3 py-1 rounded hover:bg-red-50"
              >
                Clear
              </button>
            </div>
            <div 
              ref={activityLogRef}
              className="max-h-64 overflow-y-auto space-y-2 bg-gray-50 rounded-lg p-3"
            >
              {activityLog.length === 0 ? (
                <p className="text-gray-400 text-sm text-center py-4">No activity yet</p>
              ) : (
                activityLog.map((activity, index) => (
                  <div 
                    key={index}
                    className="flex items-start gap-2 text-sm bg-white rounded p-2 border border-gray-100"
                  >
                    <span className="text-lg">{getActivityIcon(activity.type)}</span>
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-2">
                        <span className={`font-medium ${getActivityColor(activity.type)}`}>
                          {activity.city || 'System'}
                        </span>
                        <span className="text-xs text-gray-400">
                          {activity.timestamp.toLocaleTimeString()}
                        </span>
                      </div>
                      <p className="text-gray-700 break-words">{activity.message}</p>
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>
        )}
      </div>
    </main>
  );
}