// @ts-check

const lightCodeTheme = require('prism-react-renderer/themes/github');
const darkCodeTheme = require('prism-react-renderer/themes/nightOwl');

/** @type {import('@docusaurus/types').Config} */
const config = {
  title: 'compose-swr',
  url: 'https://burnoo.github.io',
  baseUrl: '/',
  organizationName: 'burnoo',
  projectName: 'compose-swr',
  deploymentBranch: "gh-pages",
  presets: [
    [
      'classic',
      /** @type {import('@docusaurus/preset-classic').Options} */
      ({
        docs: {
          editUrl: 'https://github.com/burnoo/compose-swr/tree/main/docs',
          routeBasePath: '/'
        },
      }),
    ],
  ],

  themeConfig:
    /** @type {import('@docusaurus/preset-classic').ThemeConfig} */
    ({
      navbar: {
        title: 'compose-swr',
        items: [
          {
            href: 'https://github.com/burnoo/compose-swr',
            label: 'GitHub',
            position: 'right',
          },
        ],
      },
      footer: {
        style: 'dark',
        copyright: `Copyright © ${new Date().getFullYear()} Bruno Wieczorek. Built with Docusaurus.`,
      },
      prism: {
        additionalLanguages: ['kotlin'],
        theme: lightCodeTheme,
        darkTheme: darkCodeTheme,
      },
    }),
};

module.exports = config;
