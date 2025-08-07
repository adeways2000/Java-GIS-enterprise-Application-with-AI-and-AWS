import React, { useState, useEffect } from 'react';
import { Database, Eye, Download } from 'lucide-react';

interface StacCollection {
  id: string;
  title: string;
  description: string;
  itemCount: number;
  extent: {
    spatial: number[];
    temporal: string[];
  };
  license: string;
  provider: string;
  thumbnail: string;
}

interface StacItem {
  id: string;
  collectionId: string;
  title: string;
  datetime: string;
  properties: {
    [key: string]: any;
  };
  assets: {
    [key: string]: {
      href: string;
      type: string;
      title: string;
    };
  };
  thumbnail: string;
}

export const StacCatalog: React.FC = () => {
  const [collections, setCollections] = useState<StacCollection[]>([]);
  const [selectedCollection, setSelectedCollection] = useState<StacCollection | null>(null);
  const [items, setItems] = useState<StacItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [itemsLoading, setItemsLoading] = useState(false);

  useEffect(() => {
    // Mock data for demonstration
    const mockCollections: StacCollection[] = [
      {
        id: 'sentinel-2-l2a',
        title: 'Sentinel-2 L2A',
        description: 'Sentinel-2 Level 2A atmospherically corrected surface reflectance products',
        itemCount: 1245,
        extent: {
          spatial: [6.0, 47.0, 15.0, 55.0], // Germany bounding box
          temporal: ['2020-01-01', '2024-06-30']
        },
        license: 'CC-BY-4.0',
        provider: 'European Space Agency',
        thumbnail: 'https://via.placeholder.com/300x200?text=Sentinel-2'
      },
      {
        id: 'landsat-9-l2',
        title: 'Landsat 9 Level 2',
        description: 'Landsat 9 Level 2 surface reflectance products',
        itemCount: 876,
        extent: {
          spatial: [6.0, 47.0, 15.0, 55.0], // Germany bounding box
          temporal: ['2021-09-27', '2024-06-30']
        },
        license: 'PDDL-1.0',
        provider: 'USGS',
        thumbnail: 'https://via.placeholder.com/300x200?text=Landsat-9'
      },
      {
        id: 'basf-environmental-monitoring',
        title: 'BASF Environmental Monitoring',
        description: 'Custom environmental monitoring data for BASF facilities',
        itemCount: 532,
        extent: {
          spatial: [8.4, 49.4, 8.5, 49.5], // Ludwigshafen area
          temporal: ['2022-01-01', '2024-06-30']
        },
        license: 'Proprietary',
        provider: 'BASF GmbH',
        thumbnail: 'https://via.placeholder.com/300x200?text=BASF+Monitoring'
      },
      {
        id: 'planet-scope',
        title: 'Planet Scope',
        description: 'High-resolution satellite imagery from Planet',
        itemCount: 423,
        extent: {
          spatial: [6.0, 47.0, 15.0, 55.0], // Germany bounding box
          temporal: ['2022-01-01', '2024-06-30']
        },
        license: 'Commercial',
        provider: 'Planet Labs',
        thumbnail: 'https://via.placeholder.com/300x200?text=Planet+Scope'
      }
    ];

    setTimeout(() => {
      setCollections(mockCollections);
      setLoading(false);
    }, 1000);
  }, []);

  const handleCollectionSelect = (collection: StacCollection) => {
    setSelectedCollection(collection);
    setItemsLoading(true);

    // Mock items for the selected collection
    setTimeout(() => {
      const mockItems: StacItem[] = Array.from({ length: 8 }, (_, i) => ({
        id: `${collection.id}-item-${i+1}`,
        collectionId: collection.id,
        title: `${collection.title} Item ${i+1}`,
        datetime: new Date(Date.now() - i * 86400000 * 15).toISOString().split('T')[0], // Every 15 days back
        properties: {
          'eo:cloud_cover': Math.floor(Math.random() * 30),
          'eo:bands': ['red', 'green', 'blue', 'nir'],
          'proj:epsg': 4326,
          'gsd': collection.id.includes('planet') ? 3 : collection.id.includes('sentinel') ? 10 : 30
        },
        assets: {
          'visual': {
            href: `https://example.com/${collection.id}/visual/${i+1}.tif`,
            type: 'image/tiff; application=geotiff',
            title: 'Visual (RGB)'
          },
          'data': {
            href: `https://example.com/${collection.id}/data/${i+1}.tif`,
            type: 'image/tiff; application=geotiff',
            title: 'Full Data Package'
          }
        },
        thumbnail: `https://via.placeholder.com/300x200?text=${collection.id.split('-').join('+')}+${i+1}`
      }));

      setItems(mockItems);
      setItemsLoading(false);
    }, 800);
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-bold text-gray-900 dark:text-white">
          STAC Catalog
        </h1>
        <p className="text-gray-600 dark:text-gray-400">
          Browse and search spatio-temporal asset collections
        </p>
      </div>

      {/* Main Content */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Collections Panel */}
        <div className="lg:col-span-1">
          <div className="bg-white dark:bg-gray-800 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700">
            <div className="p-4 border-b border-gray-200 dark:border-gray-700">
              <h2 className="text-lg font-semibold text-gray-900 dark:text-white flex items-center">
                <Database className="h-5 w-5 mr-2 text-blue-600 dark:text-blue-400" />
                Collections
              </h2>
            </div>

            <div className="p-4">
              <div className="relative">
                <input
                  type="text"
                  placeholder="Search collections..."
                  className="w-full pl-4 pr-4 py-2 border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white text-sm focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
              </div>
            </div>

            <div className="max-h-[calc(100vh-300px)] overflow-y-auto">
              {collections.map((collection) => (
                <div
                  key={collection.id}
                  onClick={() => handleCollectionSelect(collection)}
                  className={`p-4 border-b border-gray-200 dark:border-gray-700 cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-700 ${
                    selectedCollection?.id === collection.id ? 'bg-blue-50 dark:bg-blue-900/20' : ''
                  }`}
                >
                  <div className="flex items-start">
                    <img
                      src={collection.thumbnail}
                      alt={collection.title}
                      className="w-16 h-16 object-cover rounded mr-3 flex-shrink-0"
                    />
                    <div>
                      <h3 className="font-medium text-gray-900 dark:text-white text-sm">
                        {collection.title}
                      </h3>
                      <p className="text-xs text-gray-500 dark:text-gray-400 mt-1 line-clamp-2">
                        {collection.description}
                      </p>
                      <div className="flex items-center mt-2 text-xs text-gray-500 dark:text-gray-400">
                        <span>{collection.itemCount} items</span>
                        <span className="mx-2">•</span>
                        <span>{collection.provider}</span>
                      </div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Items Panel */}
        <div className="lg:col-span-2">
          <div className="bg-white dark:bg-gray-800 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700 h-full">
            {selectedCollection ? (
              <>
                <div className="p-4 border-b border-gray-200 dark:border-gray-700">
                  <div className="flex items-start justify-between">
                    <div>
                      <h2 className="text-lg font-semibold text-gray-900 dark:text-white">
                        {selectedCollection.title}
                      </h2>
                      <p className="text-sm text-gray-600 dark:text-gray-400 mt-1">
                        {selectedCollection.description}
                      </p>
                    </div>
                    <button className="px-3 py-1 bg-blue-600 text-white text-sm rounded-md hover:bg-blue-700">
                      Search Items
                    </button>
                  </div>

                  <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mt-4">
                    <div className="bg-gray-50 dark:bg-gray-700 p-3 rounded-md">
                      <div className="text-xs text-gray-500 dark:text-gray-400">Items</div>
                      <div className="text-lg font-semibold text-gray-900 dark:text-white">{selectedCollection.itemCount}</div>
                    </div>

                    <div className="bg-gray-50 dark:bg-gray-700 p-3 rounded-md">
                      <div className="text-xs text-gray-500 dark:text-gray-400">Provider</div>
                      <div className="text-sm font-semibold text-gray-900 dark:text-white truncate">{selectedCollection.provider}</div>
                    </div>

                    <div className="bg-gray-50 dark:bg-gray-700 p-3 rounded-md">
                      <div className="text-xs text-gray-500 dark:text-gray-400">License</div>
                      <div className="text-sm font-semibold text-gray-900 dark:text-white">{selectedCollection.license}</div>
                    </div>

                    <div className="bg-gray-50 dark:bg-gray-700 p-3 rounded-md">
                      <div className="text-xs text-gray-500 dark:text-gray-400">Temporal Range</div>
                      <div className="text-sm font-semibold text-gray-900 dark:text-white">
                        {selectedCollection.extent.temporal[0].split('T')[0]} to {selectedCollection.extent.temporal[1].split('T')[0]}
                      </div>
                    </div>
                  </div>
                </div>

                <div className="p-4">
                  <div className="flex items-center justify-between mb-4">
                    <h3 className="text-md font-semibold text-gray-900 dark:text-white">
                      Items ({items.length})
                    </h3>
                    <div className="flex space-x-2">
                      <select className="text-sm border border-gray-300 dark:border-gray-600 rounded-md bg-white dark:bg-gray-700 text-gray-900 dark:text-white focus:ring-2 focus:ring-blue-500 focus:border-transparent px-2 py-1">
                        <option>Sort by Date</option>
                        <option>Sort by Cloud Cover</option>
                        <option>Sort by Name</option>
                      </select>
                    </div>
                  </div>

                  {itemsLoading ? (
                    <div className="flex items-center justify-center h-64">
                      <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
                    </div>
                  ) : (
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                      {items.map((item) => (
                        <div key={item.id} className="border border-gray-200 dark:border-gray-700 rounded-lg overflow-hidden bg-white dark:bg-gray-800 shadow-sm hover:shadow-md transition-shadow">
                          <div className="relative h-40 bg-gray-100 dark:bg-gray-700">
                            <img
                              src={item.thumbnail}
                              alt={item.title}
                              className="w-full h-full object-cover"
                            />
                            <div className="absolute top-2 right-2 bg-black bg-opacity-50 text-white text-xs px-2 py-1 rounded">
                              {item.properties['eo:cloud_cover']}% cloud
                            </div>
                          </div>

                          <div className="p-4">
                            <h4 className="font-medium text-gray-900 dark:text-white text-sm mb-1 truncate" title={item.title}>
                              {item.title}
                            </h4>

                            <div className="flex items-center text-xs text-gray-500 dark:text-gray-400 mb-2">
                              <span>{item.datetime}</span>
                              <span className="mx-2">•</span>
                              <span>{item.properties['gsd']}m resolution</span>
                            </div>

                            <div className="flex justify-between mt-3 pt-3 border-t border-gray-100 dark:border-gray-700">
                              <button className="flex items-center text-xs text-blue-600 hover:text-blue-800 dark:text-blue-400 dark:hover:text-blue-300">
                                <Eye className="h-3 w-3 mr-1" />
                                Preview
                              </button>

                              <button className="flex items-center text-xs text-green-600 hover:text-green-800 dark:text-green-400 dark:hover:text-green-300">
                                <Download className="h-3 w-3 mr-1" />
                                Download
                              </button>
                            </div>
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              </>
            ) : (
              <div className="flex flex-col items-center justify-center h-full p-8">
                <Database className="h-16 w-16 text-gray-300 dark:text-gray-600 mb-4" />
                <h3 className="text-lg font-medium text-gray-900 dark:text-white mb-2">
                  Select a Collection
                </h3>
                <p className="text-gray-500 dark:text-gray-400 text-center max-w-md">
                  Choose a collection from the left panel to view its items and metadata.
                </p>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

