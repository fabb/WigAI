import { readFile, writeFile } from 'node:fs/promises'
import * as cheerio from 'cheerio'
import { Scraper, type ClassData, type EnumData, type Fetcher, type InterfaceData } from 'javadocs-scraper'

class MyFetcher implements Fetcher {
    url: string

    constructor(url: string) {
        this.url = url
    }

    async fetchRoot() {
        return this.fetch('index.html')
    }

    async fetch(url: string) {
        try {
            const data = await readFile(new URL(`${this.url}${url}`), 'utf-8')
            return { $: cheerio.load(data), fullUrl:url }
        } catch (error) {
            console.error(`Error fetching ${url}:`, error)
            throw error
        }
    }
}

const myFetcher = new MyFetcher('file:///Applications/Bitwig%20Studio.app/Contents/Resources/Documentation/control-surface/api/')
const scraper = Scraper.with({ fetcher: myFetcher })

const scrapedJavadocs = await scraper.scrape()

const convertClassToMarkdown = (classData: ClassData): string => {
    let markdown = `### ${[...classData.modifiers, 'class'].join(' ')} ${classData.name}\n\n`

    if (classData.description?.text) {
        markdown += `${classData.description.text}\n\n`
    }

    if (classData.extends) {
        markdown += `**Extends:** ${classData.extends.qualifiedName}\n\n`
    }

    if (classData.implements.size > 0) {
        markdown += `**Implements:** ${[...classData.implements.values()].map(impl => `\`${impl.qualifiedName}\``).join(', ')}\n\n`
    }

    const methods = classData.methods
    markdown += convertMethodsToMarkdown(methods)

    return markdown
};

const convertInterfaceToMarkdown = ( interfaceData: InterfaceData): string => {
    let markdown = `### interface ${interfaceData.name}\n\n`

    if (interfaceData.description?.text) {
        markdown += `${interfaceData.description.text}\n\n`
    }

    if (interfaceData.extends.size > 0) {
        markdown += `**Extends:** ${[...interfaceData.extends.map(extend => `\`${extend.qualifiedName}\``)].join(', ')}\n\n`
    }

    const methods = interfaceData.methods
    markdown += convertMethodsToMarkdown(methods)

    return markdown
};

const convertEnumToMarkdown = ( enumData: EnumData): string => {
    let markdown = `### enum ${enumData.name}\n\n`

    if (enumData.description?.text) {
        markdown += `${enumData.description.text}\n\n`
    }

    const values = enumData.constants
    if (values.size > 0) {
        markdown += `#### Values\n\n`
        for (const value of values) {
            markdown += `- \`${value[0]}\``
            if (value[1].description?.text) {
                markdown += `: ${value[1].description.text}`
            }
            markdown += `\n`
        }
        markdown += `\n`
    }

    const methods = enumData.methods
    markdown += convertMethodsToMarkdown(methods)


    return markdown
};

const convertMethodsToMarkdown = (methods) => {
    let markdown = ''
    if (methods.size > 0) {
        markdown += `#### Methods\n\n`
        for (const [methodName, method] of methods) {
            markdown += `##### ${methodName}\n\n`

            if (method.description?.text) {
                markdown += `${method.description.text}\n\n`
            }

            if (method.parameters.size > 0) {
                markdown += `**Parameters:**\n`
                for (const param of method.parameters) {
                    markdown += `- \`${param[1].name}\` (type ${param[1].type})`
                    if (param[1].description?.text) {
                        markdown += `: ${param[1].description.text}`
                    }
                    markdown += `\n`
                }
                markdown += `\n`
            }

            if (method.returns) {
                markdown += `**Returns:** type ${method.returns.type}`
                if (method.returns.description?.text) {
                    markdown += ` - ${method.returns.description.text}`
                }
                markdown += `\n\n`
            }
        }
    }
    return markdown
};

// convert classes, interfaces and enums to markdown
let markdownOutput = `# Bitwig API Documentation\n\n`

// Convert packages
for (const [packageName, pkg] of scrapedJavadocs.getPackages()) {
    markdownOutput += `## Package: ${packageName}\n\n`

    // // Convert classes
    // for (const [className, cls] of pkg.classes) {
    //     markdownOutput += convertClassToMarkdown(className, cls)
    // }

    // // Convert interfaces
    // for (const [interfaceName, iface] of pkg.interfaces) {
    //     markdownOutput += convertInterfaceToMarkdown(interfaceName, iface)
    // }

    // // Convert enums
    // for (const [enumName, enumObj] of pkg.enums) {
    //     markdownOutput += convertEnumToMarkdown(enumName, enumObj)
    // }

    // INFO somehow the packages in scrapedJavadocs.getPackages() do not contain the classes, interfaces and enums, so we have to iterate over them globally

    scrapedJavadocs.getClasses().forEach((classData) => {
        if (classData.package.name === packageName) {
            markdownOutput += convertClassToMarkdown(classData)
        }
    });

    scrapedJavadocs.getInterfaces().forEach((interfaceData) => {
        if (interfaceData.package.name === packageName) {
            markdownOutput += convertInterfaceToMarkdown(interfaceData)
        }
    });

    scrapedJavadocs.getEnums().forEach((enumData) => {
        if (enumData.package.name === packageName) {
            markdownOutput += convertEnumToMarkdown(enumData)
        }
    });
}

await writeFile('bitwig-api-documentation.md', markdownOutput, 'utf-8')
console.log('Documentation written to bitwig-api-documentation.md')
