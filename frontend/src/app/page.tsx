// 'use client';

// import { useState } from 'react';

// export default function Home() {
//   const [city, setCity] = useState('');
//   const [weather, setWeather] = useState<any>(null);
//   const [loading, setLoading] = useState(false);
//   const [error, setError] = useState<string | null>(null);

//   const handleSearch = async () => {
//     if (!city) return;
//     setLoading(true);
//     setWeather(null);
//     setError(null);

//     try {
//       const res = await fetch(`http://localhost:8080/api/weather/search?city=${encodeURIComponent(city)}`);
//       const data = await res.json();
//       if (data.error) {
//         setError(data.error);
//       } else {
//         setWeather(data);
//       }
//     } catch (e: any) {
//       setError("Failed to fetch from backend.");
//       console.error(e);
//     } finally {
//       setLoading(false);
//     }
//   };

//   return (
//     <main className="flex flex-col items-center justify-center min-h-screen bg-sky-50 p-6">
//       <h1 className="text-3xl font-bold text-sky-700 mb-6">🌦️ Open-Meteo Weather Search</h1>

//       <div className="flex gap-2">
//         <input
//           type="text"
//           value={city}
//           onChange={(e) => setCity(e.target.value)}
//           placeholder="Enter city name"
//           className="border border-gray-300 rounded px-3 py-2 w-64 focus:outline-none"
//         />
//         <button onClick={handleSearch} className="bg-sky-600 text-white px-4 py-2 rounded hover:bg-sky-700">
//           Search
//         </button>
//       </div>

//       {loading && <p className="mt-4">Loading...</p>}

//       {error && <p className="text-red-500 mt-4">{error}</p>}

//       {weather && !error && (
//         <div className="mt-6 bg-white p-6 rounded shadow w-96 text-center">
//           <h2 className="text-xl font-semibold mb-2">{weather.city}, {weather.country}</h2>
//           <p className="text-lg">🌡️ Current: {weather.temperature} °C</p>
//           <p className="text-lg">⬆️ Max: {weather.temperature_max} °C</p>
//           <p className="text-lg">⬇️ Min: {weather.temperature_min} °C</p>
//           <p className="text-lg">💨 Wind: {weather.windspeed} m/s</p>
//           <p className="text-lg">🌧️ Precipitation: {weather.precipitation_sum} mm</p>
//           <p className="text-lg">🌅 Sunrise: {weather.sunrise}</p>
//           <p className="text-lg">🌇 Sunset: {weather.sunset}</p>
//           <p className="text-sm text-gray-500 mt-2">Updated: {weather.time}</p>
//         </div>
//       )}
//     </main>
//   );
// }


// 'use client';
// import { useState, useEffect, useRef } from 'react';

// export default function Home() {
//   const [city, setCity] = useState('');
//   const [weather, setWeather] = useState<any>(null);
//   const [loading, setLoading] = useState(false);
//   const [error, setError] = useState<string | null>(null);
//   const [broadcasts, setBroadcasts] = useState<any[]>([]);
//   const [wsStatus, setWsStatus] = useState<'disconnected' | 'connecting' | 'connected'>('disconnected');
//   const wsRef = useRef<WebSocket | null>(null);

//   // WebSocket connection
//   useEffect(() => {
//     const connectWebSocket = () => {
//       setWsStatus('connecting');
//       const ws = new WebSocket('ws://localhost:5000');
      
//       ws.onopen = () => {
//         console.log('WebSocket connected');
//         setWsStatus('connected');
//       };

//       ws.onmessage = (event) => {
//         console.log('Received broadcast:', event.data);
//         try {
//           // Parse broadcast message: "[BROADCAST] City -> {json}"
//           const message = event.data;
//           if (message.startsWith('[BROADCAST]')) {
//             const jsonStart = message.indexOf('{');
//             if (jsonStart !== -1) {
//               const jsonStr = message.substring(jsonStart);
//               const weatherData = JSON.parse(jsonStr);
              
//               // Add to broadcasts list
//               setBroadcasts(prev => {
//                 const newBroadcasts = [
//                   { ...weatherData, timestamp: new Date() },
//                   ...prev.slice(0, 9) // Keep last 10
//                 ];
//                 return newBroadcasts;
//               });

//               // If this is the currently searched city, update main display
//               if (weather && weatherData.city === weather.city) {
//                 setWeather(weatherData);
//               }
//             }
//           }
//         } catch (e) {
//           console.error('Failed to parse broadcast:', e);
//         }
//       };

//       ws.onerror = (error) => {
//         console.error('WebSocket error:', error);
//         setWsStatus('disconnected');
//       };

//       ws.onclose = () => {
//         console.log('WebSocket disconnected, reconnecting in 3s...');
//         setWsStatus('disconnected');
//         setTimeout(connectWebSocket, 3000);
//       };

//       wsRef.current = ws;
//     };

//     connectWebSocket();

//     return () => {
//       if (wsRef.current) {
//         wsRef.current.close();
//       }
//     };
//   }, [weather]);

//   const handleSearch = async () => {
//     if (!city) return;
//     setLoading(true);
//     setWeather(null);
//     setError(null);

//     try {
//       const res = await fetch(`http://localhost:8080/api/weather/search?city=${encodeURIComponent(city)}`);
//       const data = await res.json();
//       if (data.error) {
//         setError(data.error);
//       } else {
//         setWeather(data);
//       }
//     } catch (e: any) {
//       setError("Failed to fetch from backend.");
//       console.error(e);
//     } finally {
//       setLoading(false);
//     }
//   };

//   const getWeatherIcon = (code: number) => {
//     if (code === 0) return '☀️';
//     if (code <= 3) return '🌤️';
//     if (code <= 48) return '☁️';
//     if (code <= 67) return '🌧️';
//     if (code <= 77) return '🌨️';
//     if (code <= 82) return '⛈️';
//     return '🌩️';
//   };

//   return (
//     <main className="flex flex-col items-center min-h-screen bg-gradient-to-br from-sky-100 to-blue-200 p-6">
//       <div className="w-full max-w-6xl">
//         {/* Header */}
//         <div className="text-center mb-8">
//           <h1 className="text-4xl font-bold text-sky-800 mb-2">
//             🌦️ Live Weather System
//           </h1>
//           <div className="flex items-center justify-center gap-2 text-sm">
//             <span className={`inline-block w-2 h-2 rounded-full ${
//               wsStatus === 'connected' ? 'bg-green-500' : 
//               wsStatus === 'connecting' ? 'bg-yellow-500' : 'bg-red-500'
//             }`}></span>
//             <span className="text-gray-600">
//               {wsStatus === 'connected' ? 'Live updates enabled' : 
//                wsStatus === 'connecting' ? 'Connecting...' : 'Reconnecting...'}
//             </span>
//           </div>
//         </div>

//         {/* Search Bar */}
//         <div className="flex gap-2 mb-8 justify-center">
//           <input
//             type="text"
//             value={city}
//             onChange={(e) => setCity(e.target.value)}
//             onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
//             placeholder="Enter city name"
//             className="border border-gray-300 rounded-lg px-4 py-3 w-80 focus:outline-none focus:ring-2 focus:ring-sky-500 shadow-sm"
//           />
//           <button 
//             onClick={handleSearch} 
//             disabled={loading}
//             className="bg-sky-600 text-white px-6 py-3 rounded-lg hover:bg-sky-700 disabled:opacity-50 shadow-md transition"
//           >
//             {loading ? 'Searching...' : 'Search'}
//           </button>
//         </div>

//         {error && (
//           <div className="text-center mb-6">
//             <p className="text-red-600 bg-red-50 px-4 py-2 rounded-lg inline-block">
//               {error}
//             </p>
//           </div>
//         )}

//         {/* Main Weather Display */}
//         {weather && !error && (
//           <div className="bg-white rounded-2xl shadow-xl p-8 mb-8 max-w-2xl mx-auto">
//             <div className="text-center">
//               <div className="text-6xl mb-4">
//                 {getWeatherIcon(weather.weathercode)}
//               </div>
//               <h2 className="text-3xl font-bold text-gray-800 mb-2">
//                 {weather.city}, {weather.country}
//               </h2>
//               <p className="text-5xl font-bold text-sky-600 mb-6">
//                 {weather.temperature}°C
//               </p>
              
//               <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-left">
//                 <div className="bg-sky-50 p-3 rounded-lg">
//                   <p className="text-xs text-gray-500 mb-1">High</p>
//                   <p className="text-xl font-semibold text-gray-800">
//                     ⬆️ {weather.temperature_max}°C
//                   </p>
//                 </div>
//                 <div className="bg-sky-50 p-3 rounded-lg">
//                   <p className="text-xs text-gray-500 mb-1">Low</p>
//                   <p className="text-xl font-semibold text-gray-800">
//                     ⬇️ {weather.temperature_min}°C
//                   </p>
//                 </div>
//                 <div className="bg-sky-50 p-3 rounded-lg">
//                   <p className="text-xs text-gray-500 mb-1">Wind</p>
//                   <p className="text-xl font-semibold text-gray-800">
//                     💨 {weather.windspeed} m/s
//                   </p>
//                 </div>
//                 <div className="bg-sky-50 p-3 rounded-lg">
//                   <p className="text-xs text-gray-500 mb-1">Rain</p>
//                   <p className="text-xl font-semibold text-gray-800">
//                     🌧️ {weather.precipitation_sum} mm
//                   </p>
//                 </div>
//               </div>

//               <div className="grid grid-cols-2 gap-4 mt-4">
//                 <div className="bg-orange-50 p-3 rounded-lg">
//                   <p className="text-xs text-gray-500 mb-1">Sunrise</p>
//                   <p className="text-lg font-semibold text-gray-800">
//                     🌅 {new Date(weather.sunrise).toLocaleTimeString()}
//                   </p>
//                 </div>
//                 <div className="bg-purple-50 p-3 rounded-lg">
//                   <p className="text-xs text-gray-500 mb-1">Sunset</p>
//                   <p className="text-lg font-semibold text-gray-800">
//                     🌇 {new Date(weather.sunset).toLocaleTimeString()}
//                   </p>
//                 </div>
//               </div>

//               <p className="text-xs text-gray-400 mt-4">
//                 Last updated: {new Date(weather.time).toLocaleString()}
//               </p>
//             </div>
//           </div>
//         )}

//         {/* Live Broadcasts */}
//         {broadcasts.length > 0 && (
//           <div className="max-w-6xl mx-auto">
//             <h3 className="text-2xl font-bold text-gray-800 mb-4 text-center">
//               📡 Live Broadcasts
//             </h3>
//             <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
//               {broadcasts.map((broadcast, index) => (
//                 <div 
//                   key={index}
//                   className="bg-white rounded-lg shadow-md p-4 hover:shadow-lg transition cursor-pointer"
//                   onClick={() => setWeather(broadcast)}
//                 >
//                   <div className="flex justify-between items-start mb-2">
//                     <h4 className="text-lg font-semibold text-gray-800">
//                       {broadcast.city}
//                     </h4>
//                     <span className="text-2xl">
//                       {getWeatherIcon(broadcast.weathercode)}
//                     </span>
//                   </div>
//                   <p className="text-3xl font-bold text-sky-600 mb-2">
//                     {broadcast.temperature}°C
//                   </p>
//                   <div className="text-sm text-gray-600 space-y-1">
//                     <p>💨 {broadcast.windspeed} m/s</p>
//                     <p>🌧️ {broadcast.precipitation_sum} mm</p>
//                   </div>
//                   <p className="text-xs text-gray-400 mt-2">
//                     {broadcast.timestamp.toLocaleTimeString()}
//                   </p>
//                 </div>
//               ))}
//             </div>
//           </div>
//         )}

//         {broadcasts.length === 0 && wsStatus === 'connected' && (
//           <div className="text-center text-gray-500 mt-8">
//             <p>Waiting for weather broadcasts...</p>
//             <p className="text-sm">Updates arrive every 60 seconds</p>
//           </div>
//         )}
//       </div>
//     </main>
//   );
// }

'use client';
import { useState, useEffect, useRef } from 'react';

export default function Home() {
  const [city, setCity] = useState('');
  const [weather, setWeather] = useState<any>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [broadcasts, setBroadcasts] = useState<any[]>([]);
  const [wsStatus, setWsStatus] = useState<'disconnected' | 'connecting' | 'connected'>('disconnected');
  const wsRef = useRef<WebSocket | null>(null);

  // WebSocket connection
  useEffect(() => {
    const connectWebSocket = () => {
      setWsStatus('connecting');
      const ws = new WebSocket('ws://localhost:5001'); // WebSocket bridge port
      
      ws.onopen = () => {
        console.log('WebSocket connected');
        setWsStatus('connected');
      };

      ws.onmessage = (event) => {
        console.log('Received broadcast:', event.data);
        try {
          // Parse broadcast message: "[BROADCAST] City -> {json}"
          const message = event.data;
          if (message.startsWith('[BROADCAST]')) {
            const jsonStart = message.indexOf('{');
            if (jsonStart !== -1) {
              const jsonStr = message.substring(jsonStart);
              const weatherData = JSON.parse(jsonStr);
              
              // Add to broadcasts list
              setBroadcasts(prev => {
                const newBroadcasts = [
                  { ...weatherData, timestamp: new Date() },
                  ...prev.slice(0, 9) // Keep last 10
                ];
                return newBroadcasts;
              });

              // If this is the currently searched city, update main display
              if (weather && weatherData.city === weather.city) {
                setWeather(weatherData);
              }
            }
          }
        } catch (e) {
          console.error('Failed to parse broadcast:', e);
        }
      };

      ws.onerror = (error) => {
        console.warn('WebSocket connection failed - server may not be running on port 5000');
        setWsStatus('disconnected');
      };

      ws.onclose = () => {
        console.log('WebSocket disconnected');
        setWsStatus('disconnected');
        // Only retry if we were previously connected
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

    try {
      const res = await fetch(`http://localhost:8080/api/weather/search?city=${encodeURIComponent(city)}`);
      const data = await res.json();
      if (data.error) {
        setError(data.error);
      } else {
        setWeather(data);
      }
    } catch (e: any) {
      setError("Failed to fetch from backend.");
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

  return (
    <main className="flex flex-col items-center min-h-screen bg-gradient-to-br from-sky-100 to-blue-200 p-6">
      <div className="w-full max-w-6xl">
        {/* Header */}
        <div className="text-center mb-8">
          <h1 className="text-4xl font-bold text-sky-800 mb-2">
            🌦️ Live Weather System
          </h1>
          <div className="flex items-center justify-center gap-2 text-sm">
            <span className={`inline-block w-2 h-2 rounded-full ${
              wsStatus === 'connected' ? 'bg-green-500' : 
              wsStatus === 'connecting' ? 'bg-yellow-500' : 'bg-red-500'
            }`}></span>
            <span className="text-gray-600">
              {wsStatus === 'connected' ? 'Live updates enabled' : 
               wsStatus === 'connecting' ? 'Connecting...' : 'Reconnecting...'}
            </span>
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
          <div className="max-w-6xl mx-auto">
            <h3 className="text-2xl font-bold text-gray-800 mb-4 text-center">
              📡 Live Broadcasts
            </h3>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              {broadcasts.map((broadcast, index) => (
                <div 
                  key={index}
                  className="bg-white rounded-lg shadow-md p-4 hover:shadow-lg transition cursor-pointer"
                  onClick={() => setWeather(broadcast)}
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
          <div className="text-center text-gray-500 mt-8">
            <p>Waiting for weather broadcasts...</p>
            <p className="text-sm">Updates arrive every 60 seconds</p>
          </div>
        )}
      </div>
    </main>
  );
}