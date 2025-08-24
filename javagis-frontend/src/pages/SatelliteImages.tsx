import React, { useState, useEffect } from 'react';
import {
  Upload,
  Search,
  Filter,
  Download,
  Eye,
  Trash2,
  Calendar,
  MapPin,
  Satellite,
  Info
} from 'lucide-react';
import { LoadingSpinner } from '../components/ui/LoadingSpinner';

interface SatelliteImage {
  id: string;
  filename: string;
  acquisitionDate: string;
  satellite: string;
  resolution: string;
  cloudCover: number;
  location: {
    lat: number;
    lng: number;
    name: string;
  };
  size: string;
  format: string;
  status: 'processed' | 'processing' | 'failed';
  thumbnailUrl?: string;
  downloadUrl?: string;
  metadata: {
    bands: string[];
    projection: string;
    pixelSize: string;
  };
}

export const SatelliteImages: React.FC = () => {
  const [images, setImages] = useState<SatelliteImage[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedSatellite, setSelectedSatellite] = useState('all');
  const [selectedStatus, setSelectedStatus] = useState('all');
  const [sortBy, setSortBy] = useState('acquisitionDate');
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('desc');
  const [showUploadModal, setShowUploadModal] = useState(false);

  useEffect(() => {
    fetchSatelliteImages();
  }, []);

  const fetchSatelliteImages = async () => {
    try {
      // Mock data for demonstration
      const mockImages: SatelliteImage[] = [
        {
          id: '1',
          filename: 'BASF_Ludwigshafen_20240615_Sentinel2.tif',
          acquisitionDate: '2024-06-15T10:30:00Z',
          satellite: 'Sentinel-2',
          resolution: '10m',
          cloudCover: 5.2,
          location: {
            lat: 49.4875,
            lng: 8.4660,
            name: 'BASF Ludwigshafen'
          },
          size: '245 MB',
          format: 'GeoTIFF',
          status: 'processed',
          metadata: {
            bands: ['B02', 'B03', 'B04', 'B08'],
            projection: 'EPSG:32632',
            pixelSize: '10m x 10m'
          }
        },
        {
          id: '2',
          filename: 'Rhine_Valley_20240620_Landsat8.tif',
          acquisitionDate: '2024-06-20T11:15:00Z',
          satellite: 'Landsat-8',
          resolution: '30m',
          cloudCover: 12.8,
          location: {
            lat: 49.5000,
            lng: 8.5000,
            name: 'Rhine Valley'
          },
          size: '180 MB',
          format: 'GeoTIFF',
          status: 'processed',
          metadata: {
            bands: ['B02', 'B03', 'B04', 'B05'],
            projection: 'EPSG:32632',
            pixelSize: '30m x 30m'
          }
        },
        {
          id: '3',
          filename: 'Environmental_Monitoring_20240625_PlanetScope.tif',
          acquisitionDate: '2024-06-25T09:45:00Z',
          satellite: 'PlanetScope',
          resolution: '3m',
          cloudCover: 2.1,
          location: {
            lat: 49.4800,
            lng: 8.4500,
            name: 'Environmental Monitoring Site'
          },
          size: '420 MB',
          format: 'GeoTIFF',
          status: 'processing',
          metadata: {
            bands: ['Blue', 'Green', 'Red', 'NIR'],
            projection: 'EPSG:32632',
            pixelSize: '3m x 3m'
          }
        }
      ];

      setImages(mockImages);
      setLoading(false);
    } catch (error) {
      console.error('Failed to fetch satellite images:', error);
      setLoading(false);
    }
  };

  const filteredImages = images.filter(image => {
    const matchesSearch = image.filename.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         image.location.name.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesSatellite = selectedSatellite === 'all' || image.satellite === selectedSatellite;
    const matchesStatus = selectedStatus === 'all' || image.status === selectedStatus;

    return matchesSearch && matchesSatellite && matchesStatus;
  });

  const sortedImages = [...filteredImages].sort((a, b) => {
    let aValue: any = a[sortBy as keyof SatelliteImage];
    let bValue: any = b[sortBy as keyof SatelliteImage];

    if (sortBy === 'acquisitionDate') {
      aValue = new Date(aValue);
      bValue = new Date(bValue);
    }

    if (sortOrder === 'asc') {
      return aValue > bValue ? 1 : -1;
    } else {
      return aValue < bValue ? 1 : -1;
    }
  });

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'processed':
        return 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200';
      case 'processing':
        return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200';
      case 'failed':
        return 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200';
      default:
        return 'bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-200';
    }
  };

  const UploadModal = () => (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white dark:bg-gray-800 rounded-lg p-6 w-full max-w-md">
        <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-4">
          Upload Satellite Image
        </h3>
        <div className="border-2 border-dashed border-gray-300 dark:border-gray-600 rounded-lg p-8 text-center">
          <Satellite className="h-12 w-12 text-gray-400 mx-auto mb-4" />
          <p className="text-gray-600 dark:text-gray-400 mb-2">
            Drag and drop your satellite images here
          </p>
          <p className="text-sm text-gray-500 dark:text-gray-500 mb-4">
            Supports GeoTIFF, JPEG2000, and other geospatial formats
          </p>
          <button className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700">
            Select Files
          </button>
        </div>
        <div className="flex justify-end space-x-3 mt-6">
          <button
            onClick={() => setShowUploadModal(false)}
            className="px-4 py-2 text-gray-600 dark:text-gray-400 hover:text-gray-800 dark:hover:text-gray-200"
          >
            Cancel
          </button>
          <button className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700">
            Upload
          </button>
        </div>
      </div>
    </div>
  );

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white">
            Satellite Images
          </h1>
          <p className="text-gray-600 dark:text-gray-400">
            Manage and analyze satellite imagery for environmental monitoring
          </p>
        </div>
        <button
          onClick={() => setShowUploadModal(true)}
          className="flex items-center space-x-2 px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
        >
          <Upload className="h-5 w-5" />
          <span>Upload Images</span>
        </button>
      </div>

      {/* Filters and Search */}
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700 p-6">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              Search
            </label>
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-gray-400" />
              <input
                type="text"
                placeholder="Search images..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="w-full pl-10 pr-4 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white text-sm"
              />
            </div>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              Satellite
            </label>
            <select
              value={selectedSatellite}
              onChange={(e) => setSelectedSatellite(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white text-sm"
            >
              <option value="all">All Satellites</option>
              <option value="Sentinel-2">Sentinel-2</option>
              <option value="Landsat-8">Landsat-8</option>
              <option value="PlanetScope">PlanetScope</option>
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              Status
            </label>
            <select
              value={selectedStatus}
              onChange={(e) => setSelectedStatus(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white text-sm"
            >
              <option value="all">All Status</option>
              <option value="processed">Processed</option>
              <option value="processing">Processing</option>
              <option value="failed">Failed</option>
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              Sort By
            </label>
            <select
              value={`${sortBy}-${sortOrder}`}
              onChange={(e) => {
                const [field, order] = e.target.value.split('-');
                setSortBy(field);
                setSortOrder(order as 'asc' | 'desc');
              }}
              className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white text-sm"
            >
              <option value="acquisitionDate-desc">Date (Newest)</option>
              <option value="acquisitionDate-asc">Date (Oldest)</option>
              <option value="filename-asc">Name (A-Z)</option>
              <option value="filename-desc">Name (Z-A)</option>
            </select>
          </div>
        </div>
      </div>

      {/* Images Grid */}
      <div className="grid grid-cols-1 lg:grid-cols-2 xl:grid-cols-3 gap-6">
        {sortedImages.map((image) => (
          <div key={image.id} className="bg-white dark:bg-gray-800 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700 overflow-hidden">
            {/* Image Preview */}
            <div className="h-48 bg-gray-100 dark:bg-gray-700 flex items-center justify-center">
              <Satellite className="h-16 w-16 text-gray-400" />
            </div>

            {/* Content */}
            <div className="p-4">
              <div className="flex items-start justify-between mb-2">
                <h3 className="text-sm font-medium text-gray-900 dark:text-white truncate">
                  {image.filename}
                </h3>
                <span className={`px-2 py-1 text-xs font-medium rounded-full ${getStatusColor(image.status)}`}>
                  {image.status}
                </span>
              </div>

              <div className="space-y-2 text-sm text-gray-600 dark:text-gray-400">
                <div className="flex items-center space-x-2">
                  <Calendar className="h-4 w-4" />
                  <span>{new Date(image.acquisitionDate).toLocaleDateString()}</span>
                </div>
                <div className="flex items-center space-x-2">
                  <MapPin className="h-4 w-4" />
                  <span>{image.location.name}</span>
                </div>
                <div className="flex items-center space-x-2">
                  <Info className="h-4 w-4" />
                  <span>{image.satellite} • {image.resolution} • {image.cloudCover}% cloud</span>
                </div>
              </div>

              {/* Metadata */}
              <div className="mt-3 pt-3 border-t border-gray-200 dark:border-gray-600">
                <div className="grid grid-cols-2 gap-2 text-xs text-gray-500 dark:text-gray-400">
                  <div>Size: {image.size}</div>
                  <div>Format: {image.format}</div>
                  <div>Bands: {image.metadata.bands.length}</div>
                  <div>Projection: {image.metadata.projection}</div>
                </div>
              </div>

              {/* Actions */}
              <div className="flex justify-between items-center mt-4">
                <div className="flex space-x-2">
                  <button className="p-2 text-gray-400 hover:text-blue-600 hover:bg-blue-50 dark:hover:bg-blue-900 rounded">
                    <Eye className="h-4 w-4" />
                  </button>
                  <button className="p-2 text-gray-400 hover:text-green-600 hover:bg-green-50 dark:hover:bg-green-900 rounded">
                    <Download className="h-4 w-4" />
                  </button>
                  <button className="p-2 text-gray-400 hover:text-red-600 hover:bg-red-50 dark:hover:bg-red-900 rounded">
                    <Trash2 className="h-4 w-4" />
                  </button>
                </div>
                <button className="text-xs text-blue-600 dark:text-blue-400 hover:underline">
                  View Details
                </button>
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* Empty State */}
      {sortedImages.length === 0 && (
        <div className="text-center py-12">
          <Satellite className="h-16 w-16 text-gray-400 mx-auto mb-4" />
          <h3 className="text-lg font-medium text-gray-900 dark:text-white mb-2">
            No satellite images found
          </h3>
          <p className="text-gray-600 dark:text-gray-400 mb-4">
            Upload your first satellite image to get started with geospatial analysis.
          </p>
          <button
            onClick={() => setShowUploadModal(true)}
            className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
          >
            Upload Images
          </button>
        </div>
      )}

      {/* Upload Modal */}
      {showUploadModal && <UploadModal />}
    </div>
  );
};

