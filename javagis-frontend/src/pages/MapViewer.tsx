import React, { useState, useEffect } from 'react';
import { MapContainer, TileLayer, Marker, Popup, LayersControl, FeatureGroup } from 'react-leaflet';
import type { LatLngExpression } from 'leaflet';
import {
  Layers,
  Search,
  Filter,
  Download,
  Info,
  Satellite,
  FileImage,
  ZoomIn,
  ZoomOut
} from 'lucide-react';
import 'leaflet/dist/leaflet.css';

// Fix for default markers in react-leaflet
import L from 'leaflet';

let DefaultIcon = L.divIcon({
  html: `<div style="background-color: #3B82F6; width: 12px; height: 12px; border-radius: 50%; border: 2px solid white; box-shadow: 0 2px 4px rgba(0,0,0,0.3);"></div>`,
  iconSize: [12, 12],
  iconAnchor: [6, 6],
});

L.Marker.prototype.options.icon = DefaultIcon;

interface MapLayer {
  id: string;
  name: string;
  type: 'satellite' | 'shapefile' | 'stac';
  visible: boolean;
  url?: string;
  data?: any[];
}

interface MapPoint {
  id: string;
  name: string;
  coordinates: LatLngExpression;
  type: string;
  properties: Record<string, any>;
}

export const MapViewer: React.FC = () => {
  const [center] = useState<LatLngExpression>([49.4875, 8.4660]); // Ludwigshafen, BASF headquarters
  const [zoom] = useState(10);
  const [layers, setLayers] = useState<MapLayer[]>([
    { id: '1', name: 'Environmental Monitoring Sites', type: 'satellite', visible: true },
    { id: '2', name: 'Chemical Plant Boundaries', type: 'shapefile', visible: true },
    { id: '3', name: 'Asset Tracking Points', type: 'stac', visible: false },
  ]);
  const [mapPoints, setMapPoints] = useState<MapPoint[]>([]);
  const [selectedLayer, setSelectedLayer] = useState<string>('all');
  const [searchTerm, setSearchTerm] = useState('');
  const [showLayerPanel, setShowLayerPanel] = useState(true);

  useEffect(() => {
    // Mock data for demonstration
    const mockPoints: MapPoint[] = [
      {
        id: '1',
        name: 'BASF Ludwigshafen Plant',
        coordinates: [49.4875, 8.4660],
        type: 'facility',
        properties: {
          status: 'Active',
          capacity: '100%',
          lastInspection: '2024-06-15',
          environmentalScore: 'A+'
        }
      },
      {
        id: '2',
        name: 'Rhine River Monitoring Station',
        coordinates: [49.4950, 8.4800],
        type: 'monitoring',
        properties: {
          waterQuality: 'Good',
          temperature: '18°C',
          pH: '7.2',
          lastUpdate: '2024-06-28'
        }
      },
      {
        id: '3',
        name: 'Waste Treatment Facility',
        coordinates: [49.4800, 8.4500],
        type: 'facility',
        properties: {
          status: 'Operational',
          efficiency: '95%',
          capacity: '80%',
          nextMaintenance: '2024-07-15'
        }
      },
      {
        id: '4',
        name: 'Air Quality Sensor Network',
        coordinates: [49.4920, 8.4720],
        type: 'monitoring',
        properties: {
          airQuality: 'Good',
          pm25: '12 μg/m³',
          no2: '25 μg/m³',
          lastReading: '2024-06-28 14:30'
        }
      }
    ];
    setMapPoints(mockPoints);
  }, []);

  const toggleLayer = (layerId: string) => {
    setLayers(layers.map(layer =>
      layer.id === layerId
        ? { ...layer, visible: !layer.visible }
        : layer
    ));
  };

  const filteredPoints = mapPoints.filter(point => {
    const matchesSearch = point.name.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesLayer = selectedLayer === 'all' || point.type === selectedLayer;
    return matchesSearch && matchesLayer;
  });

  const LayerPanel = () => (
    <div className={`absolute top-4 left-4 bg-white dark:bg-gray-800 rounded-lg shadow-lg border border-gray-200 dark:border-gray-700 z-[1000] transition-transform duration-300 ${
      showLayerPanel ? 'translate-x-0' : '-translate-x-full'
    }`}>
      <div className="p-4 w-80">
        <div className="flex items-center justify-between mb-4">
          <h3 className="text-lg font-semibold text-gray-900 dark:text-white">
            Map Layers
          </h3>
          <button
            onClick={() => setShowLayerPanel(!showLayerPanel)}
            className="p-1 rounded hover:bg-gray-100 dark:hover:bg-gray-700"
          >
            <Layers className="h-5 w-5 text-gray-600 dark:text-gray-400" />
          </button>
        </div>

        {/* Search */}
        <div className="mb-4">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-gray-400" />
            <input
              type="text"
              placeholder="Search locations..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full pl-10 pr-4 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white text-sm focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          </div>
        </div>

        {/* Layer Filter */}
        <div className="mb-4">
          <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            Filter by Type
          </label>
          <select
            value={selectedLayer}
            onChange={(e) => setSelectedLayer(e.target.value)}
            className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white text-sm focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          >
            <option value="all">All Types</option>
            <option value="facility">Facilities</option>
            <option value="monitoring">Monitoring Stations</option>
          </select>
        </div>

        {/* Layer Controls */}
        <div className="space-y-2">
          <h4 className="text-sm font-medium text-gray-700 dark:text-gray-300">
            Data Layers
          </h4>
          {layers.map((layer) => (
            <div key={layer.id} className="flex items-center justify-between p-2 rounded hover:bg-gray-50 dark:hover:bg-gray-700">
              <div className="flex items-center space-x-3">
                {layer.type === 'satellite' && <Satellite className="h-4 w-4 text-blue-500" />}
                {layer.type === 'shapefile' && <FileImage className="h-4 w-4 text-green-500" />}
                {layer.type === 'stac' && <Info className="h-4 w-4 text-yellow-500" />}
                <span className="text-sm text-gray-900 dark:text-white">{layer.name}</span>
              </div>
              <label className="relative inline-flex items-center cursor-pointer">
                <input
                  type="checkbox"
                  checked={layer.visible}
                  onChange={() => toggleLayer(layer.id)}
                  className="sr-only peer"
                />
                <div className="w-9 h-5 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 dark:peer-focus:ring-blue-800 rounded-full peer dark:bg-gray-600 peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-4 after:w-4 after:transition-all dark:border-gray-600 peer-checked:bg-blue-600"></div>
              </label>
            </div>
          ))}
        </div>

        {/* Statistics */}
        <div className="mt-4 pt-4 border-t border-gray-200 dark:border-gray-600">
          <div className="text-sm text-gray-600 dark:text-gray-400">
            <div className="flex justify-between">
              <span>Total Points:</span>
              <span className="font-medium">{filteredPoints.length}</span>
            </div>
            <div className="flex justify-between">
              <span>Active Layers:</span>
              <span className="font-medium">{layers.filter(l => l.visible).length}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );

  const MapControls = () => (
    <div className="absolute top-4 right-4 bg-white dark:bg-gray-800 rounded-lg shadow-lg border border-gray-200 dark:border-gray-700 z-[1000]">
      <div className="flex flex-col">
        <button className="p-3 hover:bg-gray-50 dark:hover:bg-gray-700 border-b border-gray-200 dark:border-gray-600">
          <ZoomIn className="h-5 w-5 text-gray-600 dark:text-gray-400" />
        </button>
        <button className="p-3 hover:bg-gray-50 dark:hover:bg-gray-700 border-b border-gray-200 dark:border-gray-600">
          <ZoomOut className="h-5 w-5 text-gray-600 dark:text-gray-400" />
        </button>
        <button className="p-3 hover:bg-gray-50 dark:hover:bg-gray-700 border-b border-gray-200 dark:border-gray-600">
          <Filter className="h-5 w-5 text-gray-600 dark:text-gray-400" />
        </button>
        <button className="p-3 hover:bg-gray-50 dark:hover:bg-gray-700">
          <Download className="h-5 w-5 text-gray-600 dark:text-gray-400" />
        </button>
      </div>
    </div>
  );

  return (
    <div className="h-full relative">
      {/* Header */}
      <div className="mb-4">
        <h1 className="text-2xl font-bold text-gray-900 dark:text-white">
          Interactive Map Viewer
        </h1>
        <p className="text-gray-600 dark:text-gray-400">
          BASF GmbH Environmental Monitoring & Asset Tracking
        </p>
      </div>

      {/* Map Container */}
      <div className="relative h-[calc(100vh-200px)] rounded-lg overflow-hidden border border-gray-200 dark:border-gray-700">
        <MapContainer
          center={center}
          zoom={zoom}
          style={{ height: '100%', width: '100%' }}
          className="z-0"
        >
          <LayersControl position="topright">
            <LayersControl.BaseLayer checked name="OpenStreetMap">
              <TileLayer
                attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
              />
            </LayersControl.BaseLayer>

            <LayersControl.BaseLayer name="Satellite">
              <TileLayer
                attribution='&copy; <a href="https://www.esri.com/">Esri</a>'
                url="https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}"
              />
            </LayersControl.BaseLayer>

            <LayersControl.Overlay checked name="BASF Facilities">
              <FeatureGroup>
                {filteredPoints.map((point) => (
                  <Marker key={point.id} position={point.coordinates}>
                    <Popup>
                      <div className="p-2">
                        <h3 className="font-semibold text-gray-900 mb-2">{point.name}</h3>
                        <div className="space-y-1 text-sm">
                          {Object.entries(point.properties).map(([key, value]) => (
                            <div key={key} className="flex justify-between">
                              <span className="text-gray-600 capitalize">{key}:</span>
                              <span className="font-medium">{value}</span>
                            </div>
                          ))}
                        </div>
                        <div className="mt-2 pt-2 border-t border-gray-200">
                          <span className={`px-2 py-1 text-xs font-medium rounded-full ${
                            point.type === 'facility'
                              ? 'bg-blue-100 text-blue-800'
                              : 'bg-green-100 text-green-800'
                          }`}>
                            {point.type}
                          </span>
                        </div>
                      </div>
                    </Popup>
                  </Marker>
                ))}
              </FeatureGroup>
            </LayersControl.Overlay>
          </LayersControl>
        </MapContainer>

        {/* Layer Panel */}
        <LayerPanel />

        {/* Map Controls */}
        <MapControls />

        {/* Toggle Layer Panel Button */}
        {!showLayerPanel && (
          <button
            onClick={() => setShowLayerPanel(true)}
            className="absolute top-4 left-4 p-3 bg-white dark:bg-gray-800 rounded-lg shadow-lg border border-gray-200 dark:border-gray-700 z-[1000] hover:bg-gray-50 dark:hover:bg-gray-700"
          >
            <Layers className="h-5 w-5 text-gray-600 dark:text-gray-400" />
          </button>
        )}
      </div>
    </div>
  );
};