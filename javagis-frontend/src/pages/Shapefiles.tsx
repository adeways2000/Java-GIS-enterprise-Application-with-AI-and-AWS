import React, { useState, useEffect } from 'react';
import { Upload, Search, Download, Eye, Trash2, FileImage, MapPin } from 'lucide-react';
import { LoadingSpinner } from '../components/ui/LoadingSpinner';

interface Shapefile {
  id: string;
  name: string;
  description: string;
  uploadDate: string;
  size: string;
  type: string;
  features: number;
  location: string;
  thumbnail: string;
}

export const Shapefiles: React.FC = () => {
  const [files, setFiles] = useState<Shapefile[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedType, setSelectedType] = useState<string>('all');
  const [selectedFile, setSelectedFile] = useState<Shapefile | null>(null);
  const [showModal, setShowModal] = useState(false);

  useEffect(() => {
    // Mock data for demonstration
    const mockFiles: Shapefile[] = [
      {
        id: '1',
        name: 'BASF_Ludwigshafen_Boundaries.shp',
        description: 'Plant boundaries for BASF Ludwigshafen site',
        uploadDate: '2024-06-15',
        size: '2.4 MB',
        type: 'polygon',
        features: 24,
        location: 'Ludwigshafen, Germany',
        thumbnail: 'https://via.placeholder.com/300x200?text=Plant+Boundaries'
      },
      {
        id: '2',
        name: 'Rhine_River_Monitoring_Points.shp',
        description: 'Water quality monitoring points along Rhine River',
        uploadDate: '2024-06-10',
        size: '1.2 MB',
        type: 'point',
        features: 42,
        location: 'Rhine Valley, Germany',
        thumbnail: 'https://via.placeholder.com/300x200?text=Monitoring+Points'
      },
      {
        id: '3',
        name: 'Chemical_Transport_Routes.shp',
        description: 'Transportation routes for chemical products',
        uploadDate: '2024-06-01',
        size: '3.8 MB',
        type: 'line',
        features: 156,
        location: 'Germany, France, Switzerland',
        thumbnail: 'https://via.placeholder.com/300x200?text=Transport+Routes'
      },
      {
        id: '4',
        name: 'Agricultural_Test_Fields.shp',
        description: 'Test fields for agricultural products',
        uploadDate: '2024-05-25',
        size: '4.5 MB',
        type: 'polygon',
        features: 78,
        location: 'Rhineland-Palatinate, Germany',
        thumbnail: 'https://via.placeholder.com/300x200?text=Agricultural+Fields'
      },
      {
        id: '5',
        name: 'Environmental_Monitoring_Grid.shp',
        description: 'Grid system for environmental monitoring',
        uploadDate: '2024-05-20',
        size: '1.8 MB',
        type: 'polygon',
        features: 120,
        location: 'Ludwigshafen, Germany',
        thumbnail: 'https://via.placeholder.com/300x200?text=Monitoring+Grid'
      },
      {
        id: '6',
        name: 'Facility_Locations.shp',
        description: 'BASF facility locations in Europe',
        uploadDate: '2024-05-15',
        size: '0.9 MB',
        type: 'point',
        features: 32,
        location: 'Europe',
        thumbnail: 'https://via.placeholder.com/300x200?text=Facility+Locations'
      }
    ];

    setTimeout(() => {
      setFiles(mockFiles);
      setLoading(false);
    }, 1000);
  }, []);

  const filteredFiles = files.filter(file => {
    const matchesSearch = file.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
                          file.description.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesType = selectedType === 'all' || file.type === selectedType;

    return matchesSearch && matchesType;
  });

  const fileTypes = ['point', 'line', 'polygon'];

  const handleViewFile = (file: Shapefile) => {
    setSelectedFile(file);
    setShowModal(true);
  };

  const handleDeleteFile = (id: string) => {
    setFiles(files.filter(file => file.id !== id));
  };

  const FileModal = () => {
    if (!selectedFile) return null;

    return (
      <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
        <div className="bg-white dark:bg-gray-800 rounded-lg shadow-xl max-w-4xl w-full max-h-[90vh] overflow-hidden">
          <div className="flex justify-between items-center p-4 border-b border-gray-200 dark:border-gray-700">
            <h3 className="text-lg font-semibold text-gray-900 dark:text-white">
              {selectedFile.name}
            </h3>
            <button
              onClick={() => setShowModal(false)}
              className="text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200"
            >
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>

          <div className="p-4 overflow-y-auto max-h-[calc(90vh-8rem)]">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <img
                  src={selectedFile.thumbnail}
                  alt={selectedFile.name}
                  className="w-full h-auto rounded-lg"
                />
              </div>

              <div className="space-y-4">
                <div>
                  <h4 className="text-sm font-medium text-gray-500 dark:text-gray-400">Description</h4>
                  <p className="mt-1 text-gray-900 dark:text-white">{selectedFile.description}</p>
                </div>

                <div>
                  <h4 className="text-sm font-medium text-gray-500 dark:text-gray-400">Details</h4>
                  <div className="mt-2 space-y-2">
                    <div className="flex justify-between">
                      <span className="text-gray-600 dark:text-gray-400">Upload Date:</span>
                      <span className="font-medium text-gray-900 dark:text-white">{selectedFile.uploadDate}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-gray-600 dark:text-gray-400">Type:</span>
                      <span className="font-medium text-gray-900 dark:text-white capitalize">{selectedFile.type}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-gray-600 dark:text-gray-400">Features:</span>
                      <span className="font-medium text-gray-900 dark:text-white">{selectedFile.features}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-gray-600 dark:text-gray-400">Size:</span>
                      <span className="font-medium text-gray-900 dark:text-white">{selectedFile.size}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-gray-600 dark:text-gray-400">Location:</span>
                      <span className="font-medium text-gray-900 dark:text-white">{selectedFile.location}</span>
                    </div>
                  </div>
                </div>

                <div className="pt-4 border-t border-gray-200 dark:border-gray-700">
                  <h4 className="text-sm font-medium text-gray-500 dark:text-gray-400">Actions</h4>
                  <div className="mt-2 flex space-x-2">
                    <button className="flex items-center px-3 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700">
                      <Eye className="h-4 w-4 mr-2" />
                      View in Map
                    </button>
                    <button className="flex items-center px-3 py-2 bg-green-600 text-white rounded-md hover:bg-green-700">
                      <Download className="h-4 w-4 mr-2" />
                      Download
                    </button>
                    <button className="flex items-center px-3 py-2 bg-red-600 text-white rounded-md hover:bg-red-700">
                      <Trash2 className="h-4 w-4 mr-2" />
                      Delete
                    </button>
                  </div>
                </div>
              </div>
            </div>

            <div className="mt-6 pt-6 border-t border-gray-200 dark:border-gray-700">
              <h4 className="text-sm font-medium text-gray-500 dark:text-gray-400 mb-4">Attribute Table Preview</h4>
              <div className="overflow-x-auto">
                <table className="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
                  <thead className="bg-gray-50 dark:bg-gray-700">
                    <tr>
                      <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">ID</th>
                      <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Name</th>
                      <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Type</th>
                      <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Area (m²)</th>
                      <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Status</th>
                    </tr>
                  </thead>
                  <tbody className="bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-700">
                    {[...Array(5)].map((_, i) => (
                      <tr key={i}>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">{i + 1}</td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">Feature {i + 1}</td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white capitalize">{selectedFile.type}</td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">{Math.floor(Math.random() * 10000)}</td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm">
                          <span className={`px-2 py-1 text-xs font-medium rounded-full ${
                            i % 3 === 0 ? 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-300' :
                            i % 3 === 1 ? 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-300' :
                            'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-300'
                          }`}>
                            {i % 3 === 0 ? 'Active' : i % 3 === 1 ? 'Pending' : 'Monitoring'}
                          </span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </div>

          <div className="flex justify-end p-4 border-t border-gray-200 dark:border-gray-700">
            <button
              onClick={() => setShowModal(false)}
              className="px-4 py-2 bg-gray-200 text-gray-800 rounded-md hover:bg-gray-300 dark:bg-gray-700 dark:text-gray-200 dark:hover:bg-gray-600"
            >
              Close
            </button>
          </div>
        </div>
      </div>
    );
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <LoadingSpinner />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 dark:text-white">
            Shapefiles
          </h1>
          <p className="text-gray-600 dark:text-gray-400">
            Manage and analyze geospatial vector data
          </p>
        </div>
        <button className="flex items-center px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700">
          <Upload className="h-5 w-5 mr-2" />
          Upload Shapefile
        </button>
      </div>

      {/* Filters */}
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700 p-4">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {/* Search */}
          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
              Search
            </label>
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-gray-400" />
              <input
                type="text"
                placeholder="Search by name or description..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="w-full pl-10 pr-4 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white text-sm focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>
          </div>

          {/* Type Filter */}
          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
              Geometry Type
            </label>
            <select
              value={selectedType}
              onChange={(e) => setSelectedType(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white text-sm focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            >
              <option value="all">All Types</option>
              {fileTypes.map((type) => (
                <option key={type} value={type} className="capitalize">{type}</option>
              ))}
            </select>
          </div>
        </div>
      </div>

      {/* Results */}
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700">
        <div className="p-4 border-b border-gray-200 dark:border-gray-700 flex justify-between items-center">
          <h2 className="text-lg font-semibold text-gray-900 dark:text-white">
            {filteredFiles.length} Shapefiles Found
          </h2>
          <div className="flex space-x-2">
            <button className="flex items-center px-3 py-1 text-sm bg-gray-100 text-gray-700 rounded-md hover:bg-gray-200 dark:bg-gray-700 dark:text-gray-300 dark:hover:bg-gray-600">
              <Download className="h-4 w-4 mr-1" />
              Export List
            </button>
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 p-4">
          {filteredFiles.map((file) => (
            <div key={file.id} className="border border-gray-200 dark:border-gray-700 rounded-lg overflow-hidden bg-white dark:bg-gray-800 shadow-sm hover:shadow-md transition-shadow">
              <div className="relative h-48 bg-gray-100 dark:bg-gray-700">
                <img
                  src={file.thumbnail}
                  alt={file.name}
                  className="w-full h-full object-cover"
                />
                <div className="absolute top-2 right-2 bg-black bg-opacity-50 text-white text-xs px-2 py-1 rounded capitalize">
                  {file.type}
                </div>
              </div>

              <div className="p-4">
                <h3 className="font-medium text-gray-900 dark:text-white text-sm mb-1 truncate" title={file.name}>
                  {file.name}
                </h3>

                <p className="text-xs text-gray-500 dark:text-gray-400 mb-2 line-clamp-2" title={file.description}>
                  {file.description}
                </p>

                <div className="flex items-center text-xs text-gray-500 dark:text-gray-400 mb-2">
                  <FileImage className="h-3 w-3 mr-1" />
                  {file.features} features
                  <span className="mx-2">•</span>
                  <MapPin className="h-3 w-3 mr-1" />
                  {file.location}
                </div>

                <div className="flex items-center justify-between text-xs">
                  <span className="text-gray-500 dark:text-gray-400">{file.uploadDate}</span>
                  <span className="text-gray-500 dark:text-gray-400">{file.size}</span>
                </div>

                <div className="flex justify-between mt-4 pt-4 border-t border-gray-100 dark:border-gray-700">
                  <button
                    onClick={() => handleViewFile(file)}
                    className="flex items-center text-xs text-blue-600 hover:text-blue-800 dark:text-blue-400 dark:hover:text-blue-300"
                  >
                    <Eye className="h-3 w-3 mr-1" />
                    View
                  </button>

                  <button className="flex items-center text-xs text-green-600 hover:text-green-800 dark:text-green-400 dark:hover:text-green-300">
                    <Download className="h-3 w-3 mr-1" />
                    Download
                  </button>

                  <button
                    onClick={() => handleDeleteFile(file.id)}
                    className="flex items-center text-xs text-red-600 hover:text-red-800 dark:text-red-400 dark:hover:text-red-300"
                  >
                    <Trash2 className="h-3 w-3 mr-1" />
                    Delete
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>

        {filteredFiles.length === 0 && (
          <div className="p-8 text-center">
            <p className="text-gray-500 dark:text-gray-400">No shapefiles found matching your criteria.</p>
          </div>
        )}
      </div>

      {showModal && <FileModal />}
    </div>
  );
};